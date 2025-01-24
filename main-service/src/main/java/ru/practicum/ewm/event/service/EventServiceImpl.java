package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.enums.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import ru.practicum.webclient.StatsClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor  //активирует "private final TestStorage testStorage",аналог @Autowired, требует org.projectlombok в pom dependacys
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;


    @Value("${ewm.service.name}")
    private String serviceName;

    @Override
    public List<EventShortDto> getEvents_1(String text, List<Long> categories, Boolean paid,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Boolean onlyAvailable, SortType sort,
                                           Integer from, Integer size, HttpServletRequest request) {

        /*
!!        это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события
!! текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв
если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени
информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие
!! информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
         */

        //базовые проверки
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Дата окончания rangeEnd раньше даты начала события rangeStart.");
        }

        //определяем сортировку и пагинацию
        Sort sortAsc = switch (sort) {
            case SortType.EVENT_DATE -> Sort.by(Sort.Direction.DESC, "eventDate");
            //resultList.sort(Comparator.comparing(EventShortDto::getEventDate));
            case SortType.VIEWS -> Sort.by("views");
            //resultList.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        };

        //Sort sortAsc = Sort.by("createdOn"); //сортируем от старых к новым при извлечении
        //?-Pageable pageable = PageRequest.of(input.getFrom() / input.getSize(), input.getSize(), sort);

        PageRequest pagenation = PageRequest.of(from / size, size, sortAsc);


        //подготовка и получение данных из репозитория через Query DSL
        //BooleanExpression conditions = QEvent.event.id.ne(-1L); //инициализируем стартовым условием
        BooleanExpression conditions = QEvent.event.state.in(State.PUBLISHED); //инициализируем стартовым условием

        //text,paid,onlyAvailable,

        if (text != null && !text.isBlank()) {
            String s = "%" + text.trim() + "%";
            conditions = conditions.and(QEvent.event.annotation.likeIgnoreCase(s))
                    .or(QEvent.event.description.likeIgnoreCase(s));

        }

        if (categories != null) {
            conditions = conditions.and(QEvent.event.category.id.in(categories));
        }

        if (onlyAvailable) {
            conditions = conditions.and(QEvent.event.confirmedRequests.loe(QEvent.event.participantLimit));
        }

        if (paid != null) {
            conditions = conditions.and(QEvent.event.paid.eq(paid));
        }

        if (rangeStart != null) {
            conditions = QEvent.event.eventDate.after(rangeStart); //дата и время не раньше которых должно произойти событие

        }

        if (rangeEnd != null) {
            conditions = QEvent.event.eventDate.before(rangeEnd); //дата и время не позже которых должно произойти событие
        }

        if (rangeStart == null && rangeEnd == null) {
            //если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно
            //выгружать события, которые произойдут позже текущей даты и времени
            conditions = QEvent.event.eventDate.after(LocalDateTime.now());
        }

        List<Event> eventsList = eventRepository.findAll(conditions, pagenation).getContent();

        List<Event> events = getRefreshViewsAndConfirms(eventsList, true, true);

        /* ? -
                if (events.isEmpty()) {
            return new ArrayList<>();
        }

         */
/*
        //обновим Long views (кол-во просмотрев события) Long views
        List<String> urisEventId = events
                .stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        //?-String uris = String.join(", ", urisEventId);


        List<ViewStatsDto> statsList = (List<ViewStatsDto>) statsClient.getStats(events.getFirst().getCreatedOn().minusDays(1),
                LocalDateTime.now(), urisEventId, false);

        //обновим Long confirmedRequests (количество одобренных заявок на участие в данном событии)
        List<Long> idEventList = events.stream().
                map(Event::getId)
                .toList();

        List<Request> confirmsList = requestRepository.findAllByEventIdInAndStatusIs(idEventList, StatusRequest.CONFIRMED);

        ///для каждого Event обновим views (кол-во просмотрев события) и confirmedRequests(кол-во одобренных заявок)
        events.forEach(e -> {
            String s = "/events/" + e.getId();

            Optional<ViewStatsDto> v = statsList.stream()
                    .filter(o -> o.getUri().equals(s)).findFirst();

            if (v.isPresent()) {
                e.setViews(v.get().getHits()); //обновили счётчик
            }

            Long c = confirmsList.stream()
                    .filter(o -> o.getEvent().getId().equals(e.getId()))
                    .collect(Collectors.counting());  //? - .count();

            if (c != null) {
                e.setConfirmedRequests(c); //обновили счётчик
            }


        });

*/
        StatsClient statsClient = new StatsClient("http://localhost:9090", new RestTemplateBuilder());


        try {
            EndpointHitDto endpointHitDto = EndpointHitDto
                    .builder()
                    .app(this.serviceName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.create(endpointHitDto);

            //log.info(""); System.out.println("!!! LocalDateTime.now():" + LocalDateTime.now());

        } catch (BadStatServiceException e) {
            e.getMessage();
        }
/*
        EndpointHitDto endpointHitDto3 = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/20")
                .ip("111.222.100.112")
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.create(endpointHitDto1);

 */

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    //@Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getEvents_2(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        System.out.println("!!! 1 LocalDateTime.now(): " + LocalDateTime.now());
        /*
        Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия
        В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
        https://practicum.yandex.ru/learn/java-developer/courses/60333a68-3543-4606-9414-a967cc8391db/sprints/390249/topics/5b552eda-c38e-4a1f-aa6d-069327ab5204/lessons/6a0dfee0-11d7-48d3-9da9-7da4fb11513d/

         */
        //Sort.unsorted()
        //Sort sortCreatedOnAsc = Sort.by("createdOn"); //сортируем от старых к новым при извлечении
        Sort sortCreatedOnAsc = Sort.by(Sort.Direction.DESC, "createdOn"); //сортируем от старых к новым при извлечении

        //?-Pageable pageable = PageRequest.of(input.getFrom() / input.getSize(), input.getSize(), sort);
        PageRequest pagenation = PageRequest.of(from / size, size, sortCreatedOnAsc);


        //базовые проверки
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Дата окончания rangeEnd раньше даты начала события rangeStart.");
        }
        System.out.println("!!! 2 LocalDateTime.now(): " + LocalDateTime.now());
        //подготовка и получение данных из репозитория через Query DSL
        BooleanExpression conditions = QEvent.event.id.ne(-1L); //инициализируем стартовым условием
        if (users != null) {
            conditions = conditions.and(QEvent.event.initiator.id.in(users));
        }
        if (states != null) {
            conditions = conditions.and(QEvent.event.state.in(states));
        }
        if (categories != null) {
            conditions = conditions.and(QEvent.event.category.id.in(categories));
        }

        if (rangeStart != null) {
            conditions = QEvent.event.eventDate.after(rangeStart); //дата и время не раньше которых должно произойти событие

        }

        if (rangeEnd != null) {
            conditions = QEvent.event.eventDate.before(rangeEnd); //дата и время не позже которых должно произойти событие
        }

        if (rangeStart == null && rangeEnd == null) {
            //если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно
            //выгружать события, которые произойдут позже текущей даты и времени
            conditions = QEvent.event.eventDate.after(LocalDateTime.now());
        }
/*


 */
        System.out.println("!!! 3 LocalDateTime.now(): " + LocalDateTime.now());
        List<Event> eventsList = eventRepository.findAll(conditions, pagenation).getContent();
        System.out.println("!!! 4 LocalDateTime.now(): " + LocalDateTime.now());
        List<Event> events = getRefreshViewsAndConfirms(eventsList, true, false);
/*
        //обновим Long views (кол-во просмотрев события) Long views
        List<String> urisEventId = events
                .stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        //?-String uris = String.join(", ", urisEventId);

        StatsClient statsClient = new StatsClient("http://localhost:9090", new RestTemplateBuilder());

        List<ViewStatsDto> statsList = (List<ViewStatsDto>) statsClient.getStats(events.getFirst().getCreatedOn().minusDays(1),
                LocalDateTime.now(), urisEventId, false);

        //обновим Long confirmedRequests (количество одобренных заявок на участие в данном событии)
        List<Long> idEventList = events.stream().
                map(Event::getId)
                .toList();

        List<Request> confirmsList = requestRepository.findAllByEventIdInAndStatusIs(idEventList, StatusRequest.CONFIRMED);

        ///для каждого Event обновим views (кол-во просмотрев события) и confirmedRequests(кол-во одобренных заявок)
        events.forEach(e -> {
            String s = "/events/" + e.getId();

            Optional<ViewStatsDto> v = statsList.stream()
                    .filter(o -> o.getUri().equals(s)).findFirst();

            if (v.isPresent()) {
                e.setViews(v.get().getHits()); //обновили счётчик
            }

            Long c = confirmsList.stream()
                    .filter(o -> o.getEvent().getId().equals(e.getId()))
                    .collect(Collectors.counting());  //? - .count();

            if (c != null) {
                e.setConfirmedRequests(c); //обновили счётчик
            }


        });
*/
        System.out.println("!!! 13 LocalDateTime.now(): " + LocalDateTime.now());
        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());

                //EventMapper.toEventFullDto(eventRepository.save(event)); ..List.of();


        //event.setConfirmedRequests(countConfirm);

        /*

        List<String> urisList = events
                .stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        String uris = String.join(", ", urisList);

        List<StatsDto> statsList = statClient.getStats(events.getFirst().getCreatedOn().minusSeconds(1),
                LocalDateTime.now(), uris, false);



        var ids = events.stream().map(Event::getId).toList();
        Map<Long, List<ParticipationRequest>> confirmedRequests = requestService.prepareConfirmedRequests(ids);

        return events.stream().map(event -> {

                    Optional<StatsDto> stat = statsList.stream()
                            .filter(statsDto -> statsDto.getUri().equals("/events/" + event.getId()))
                            .findFirst();
                    var requests = confirmedRequests.get(event.getId());
                    var r = EventMapper.mapToFullDto(event, stat.isPresent() ? stat.get().getHits() : 0L);

                    r.setConfirmedRequests(requests != null ? requests.size() : 0);
                    return r;
                })
                .toList();
         */

    }
    @Transactional
    @Override
    public EventFullDto updateEvent_1(Long eventId, UpdateEventAdminRequest obj) {
        /*
        Редактирование данных любого события администратором. Валидация данных не требуется. Обратите внимание:


*/

        //базовые проверки
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        /*
        Category category = categoryRepository.findFirst1ById(obj.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + obj.getCategory() + " was not found"));


         */

        //дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
        if (obj.getEventDate() != null) {
            if (!obj.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила.");
                //throw new WrongEventDateException("Field: eventDate. Error: должно содержать дату, которая еще не наступила.");
            }
        }



        //событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
        if (obj.getStateAction() == StateAction.PUBLISH_EVENT) {
            if (event.getState() != State.PENDING) {
                throw new WrongConditionsException("Cannot publish the event because it's not in the right state: PUBLISHED");
            }
        }

        //событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
        if (obj.getStateAction() != null) {
            if (obj.getStateAction() == StateAction.REJECT_EVENT && event.getState() == State.PUBLISHED) {
                //if (event.getState() == State.PUBLISHED) {
                throw new WrongConditionsException("Событие можно отклонить, только если оно еще не опубликовано");
                //}
            }
        }



        //обновление изменяемых полей

        if (obj.getAnnotation() != null && !obj.getAnnotation().isBlank()) {
            event.setAnnotation(obj.getAnnotation());
        }
        if (obj.getCategory() != null) {
            event.setCategory(categoryRepository.findFirst1ById(obj.getCategory()).get());
        }

        //private Long confirmedRequests;
        Long countConfirm = (long) requestRepository.findAllByEventIdAndStatusIs(eventId, StatusRequest.CONFIRMED).size();
        event.setConfirmedRequests(countConfirm);

        if (obj.getDescription() != null && !obj.getDescription().isBlank()) {
            event.setDescription(obj.getDescription());
        }
        if (obj.getEventDate() != null) {
            event.setEventDate(obj.getEventDate());
        }
        if (obj.getLocation() != null) {
            locationRepository.save(obj.getLocation());
            //Location location = locationRepository.save(newEventDto.getLocation());
            //Event event = EventMapper.toEvent(newEventDto, category, user, location);

            event.setLocation(obj.getLocation());
        }
        if (obj.getPaid() != null) {
            event.setPaid(obj.getPaid());
        }
        if (obj.getParticipantLimit() != null) {
            event.setParticipantLimit(obj.getParticipantLimit());
        }
        if (obj.getRequestModeration() != null) {
            event.setRequestModeration(obj.getRequestModeration());
        }
        if (obj.getStateAction() != null) {
            if (obj.getStateAction() == StateAction.CANCEL_REVIEW) {
                event.setState(State.CANCELED);
            }
            if (obj.getStateAction() == StateAction.SEND_TO_REVIEW) {
                event.setState(State.PENDING);
            }
            if (obj.getStateAction() == StateAction.REJECT_EVENT) {
                event.setState(State.CANCELED);
            }
            if (obj.getStateAction() == StateAction.PUBLISH_EVENT) {
                event.setPublishedOn(LocalDateTime.now());
                event.setState(State.PUBLISHED);
            }
        }
        if (obj.getTitle() != null && !obj.getTitle().isBlank()) {
            event.setTitle(obj.getTitle());
        }

        /*
                event = eventRepository.save(event);

        Optional<StatsDto> stat = statClient.getStats(event.getCreatedOn().minusSeconds(1), LocalDateTime.now(),
                        "/events/" + event.getId(), false).stream().findFirst();

        EventFullDto result = EventMapper.mapToFullDto(event, stat.isPresent() ? stat.get().getHits() : 0L);

        List<ParticipationRequest> confirmedRequests = requestService
                .prepareConfirmedRequests(List.of(event.getId())).get(event.getId());
        result.setConfirmedRequests(confirmedRequests != null ? confirmedRequests.size() : 0);

         */

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getEvents(Long userId, Integer from, Integer size) {

/*Вариант 1
        Sort sortCreatedOnAsc = Sort.by("createdOn"); //сортируем от старых к новым при извлечении

        PageRequest pagenation = PageRequest.of(from / size, size, sortCreatedOnAsc);

        //базовые проверки
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        List<Event> events = eventRepository.findAllByInitiator(user, pagenation);

        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());

+ надо добавить блок "обновим views и confirmedRequests"    ?-вынести в отдельный метод

 */
        //Вариант 2 - переиспользуем getEvents_2
        return this.getEvents_2(List.of(userId), null, null, LocalDateTime.now().minusYears(100L), LocalDateTime.now().plusYears(100L), from, size);


    }

    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        //Обратите внимание: дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента

        if (!newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            //throw new WrongEventDateException("Field: eventDate. Error: должно содержать дату, которая еще не наступила.");
            throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила.");

        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));


        Category category = categoryRepository.findFirst1ById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));

        Location location = locationRepository.save(newEventDto.getLocation());
        Event event = EventMapper.toEvent(newEventDto, category, user, location);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        //базовые проверки
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        //? - + надо добавить блок "обновим views и confirmedRequests"    ?-вынести в отдельный метод



        return EventMapper.toEventFullDto(eventRepository.findAllByInitiatorAndId(user, eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found")));

    }

    @Transactional
    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest obj) {

        /*
    изменить можно только отмененные события  State.CANCELED
    или события в состоянии ожидания модерации State.PENDING(Ожидается код ошибки 409)
    дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)

        */
        //базовые проверки
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
/*
        Category category = categoryRepository.findFirst1ById(obj.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + obj.getCategory() + " was not found"));


 */

        if (event.getState() != State.CANCELED && event.getState() != State.PENDING) {
            throw new WrongConditionsException("Only pending or canceled events can be changed");

        }

        if (obj.getEventDate() != null) {
            if (!obj.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
                //throw new WrongEventDateException("Field: eventDate. Error: должно содержать дату, которая еще не наступила.");

                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила.");
            }
        }

        //обновление изменяемых полей

        if (obj.getAnnotation() != null && !obj.getAnnotation().isBlank()) {
            event.setAnnotation(obj.getAnnotation());
        }
        if (obj.getCategory() != null) {
            //Category category = categoryRepository.findFirst1ById(obj.getCategory()).get();
            event.setCategory(categoryRepository.findFirst1ById(obj.getCategory()).get());
        }

        Long countConfirm = (long) requestRepository.findAllByEventIdAndStatusIs(eventId, StatusRequest.CONFIRMED).size();
        event.setConfirmedRequests(countConfirm);

        if (obj.getDescription() != null && !obj.getDescription().isBlank()) {
            event.setDescription(obj.getDescription());
        }
        if (obj.getEventDate() != null) {
            event.setEventDate(obj.getEventDate());
        }
        if (obj.getLocation() != null) {
            event.setLocation(obj.getLocation());
        }
        if (obj.getPaid() != null) {
            event.setPaid(obj.getPaid());
        }
        if (obj.getParticipantLimit() != null) {
            event.setParticipantLimit(obj.getParticipantLimit());
        }
        if (obj.getRequestModeration() != null) {
            event.setRequestModeration(obj.getRequestModeration());
        }
        if (obj.getStateAction() != null) {
            if (obj.getStateAction() == StateAction.CANCEL_REVIEW) {
                event.setState(State.CANCELED);
            }
            if (obj.getStateAction() == StateAction.SEND_TO_REVIEW) {
                event.setState(State.PENDING);
            }
        }
        if (obj.getTitle() != null && !obj.getTitle().isBlank()) {
            event.setTitle(obj.getTitle());
        }


        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        //базовые проверки
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

/* ? -


 */
        //? - + надо добавить блок "обновим views и confirmedRequests"    ?-вынести в отдельный метод



        List<ParticipationRequestDto> requestList = requestRepository.findAllByEventInitiatorAndEvent(user, event).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());

        return requestList;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest obj) {
                /*
если для события лимит заявок  равен participantLimit=0 или отключена пре-модерация заявок requestModeration=false, то подтверждение заявок не требуется
статус можно изменить только у заявок, находящихся в состоянии ожидания  Request.status=StatusRequest.PENDING(Ожидается код ошибки 409)
если при подтверждении данной заявки, лимит заявок для события исчерпан Event.participantLimit=Event.confirmedRequests, то все неподтверждённые заявки необходимо отклонить

    private List<Long> requestIds; //Идентификаторы запросов на участие в событии текущего пользователя
    private Status status; //Новый статус запроса на участие в событии тек. пользователя CONFIRMED или REJECTED

Status.CONFIRMED, Status.REJECTED
         */
        //базовые проверки
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (obj.getStatus() != Status.CONFIRMED && obj.getStatus() != Status.REJECTED) {
            throw new BadRequestException("Поступил некорректный статус" + obj.getStatus());

        }

        boolean newStatusConfirmed;
        if (obj.getStatus() == Status.CONFIRMED ) {
            newStatusConfirmed = true;
        } else {
            newStatusConfirmed = false;
        }



        //нельзя подтвердить заявку, если уже достигнут лимит по заявкам
        // на данное событие Event.participantLimit=Event.confirmedRequests(Ожидается код ошибки 409)
        long partLim = event.getParticipantLimit();
        long confirmCnt = event.getConfirmedRequests();
        if (partLim == confirmCnt) {
            throw new WrongConditionsException("The participant limit has been reached");

        }



        //?-List<Request> requestList = requestRepository.findAllByIdAndStatusIs(obj.getRequestIds(), StatusRequest.PENDING);
        List<Request> requestList = requestRepository.findAllById(obj.getRequestIds());

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (Request r : requestList) {
            if (r.getStatus() != StatusRequest.PENDING) {
                throw new BadRequestException("Request must have status PENDING");

            }

            //нельзя подтвердить заявку, если уже достигнут лимит по заявкам - отклоняем
            long confirm = event.getConfirmedRequests();
            if (partLim == confirm && partLim > 0) {
                r.setStatus(StatusRequest.REJECTED);

                rejectedRequests.add(RequestMapper.toParticipationRequestDto(r));
            } else if (newStatusConfirmed) {
                r.setStatus(StatusRequest.CONFIRMED);
                Long tmp = event.getConfirmedRequests() + 1;
                event.setConfirmedRequests(tmp); //инкримент счётчика

                eventRepository.save(event); //?-или eventRepository.save(getRefreshViewsAndConfirms(List.of(event)).getFirst());
                confirmedRequests.add(RequestMapper.toParticipationRequestDto(r));
            } else if (!newStatusConfirmed) {
                r.setStatus(StatusRequest.REJECTED);
                rejectedRequests.add(RequestMapper.toParticipationRequestDto(r));
            }

        }


        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }


    @Override
    public EventFullDto getEvent_1(Long id, HttpServletRequest request) {
        /*
        событие должно быть опубликовано
        информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
        информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
         */
        //базовые проверки
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Посмотреть можно только опубликованное событие.");
        }


        Event result = getRefreshViewsAndConfirms(List.of(event), true, true).getFirst();


        StatsClient statsClient = new StatsClient("http://localhost:9090", new RestTemplateBuilder());
        try {
            EndpointHitDto endpointHitDto = EndpointHitDto
                    .builder()
                    .app(this.serviceName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.create(endpointHitDto);
            //log.info("Сохранение статистики.");
        } catch (BadStatServiceException e) {
            e.getMessage();
        }
/*
        EndpointHitDto endpointHitDto3 = EndpointHitDto.builder()
                .app("main-service")
                .uri("/events/20")
                .ip("111.222.100.112")
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.create(endpointHitDto1);

 */

        return EventMapper.toEventFullDto(result);
        //return null;
    }


    public List<Event> getRefreshViewsAndConfirms(List<Event> events, Boolean getViews, Boolean getConfirms) {

        if (events.isEmpty()) {
            return null;
        }


        //обновим Long views (кол-во просмотрев события) Long views
        if (getViews) {

            System.out.println("!!! 5 LocalDateTime.now(): " + LocalDateTime.now());
            //обновим Long views (кол-во просмотрев события) Long views
            List<String> urisEventId = events
                    .stream()
                    .map(event -> "/events/" + event.getId())
                    .toList();
            //?-String uris = String.join(", ", urisEventId);
            System.out.println("!!! 6 LocalDateTime.now(): " + LocalDateTime.now());
            StatsClient statsClient = new StatsClient("http://localhost:9090", new RestTemplateBuilder());

            System.out.println("!!! 7 LocalDateTime.now(): " + LocalDateTime.now());
            Object responseBody = statsClient.getStats(events.getFirst().getCreatedOn().minusDays(1),
                    LocalDateTime.now(), urisEventId, true).getBody();

            System.out.println("!!! 8 LocalDateTime.now(): " + LocalDateTime.now());
            List<ViewStatsDto> statsList = new ArrayList<>();
            if (responseBody != null) {

                List<Map<String, Object>> body = (List<Map<String, Object>>) responseBody;
                if (body != null && body.size() > 0) {
                    for (Map<String, Object> s : body) {
                        ViewStatsDto viewStats = ViewStatsDto.builder()
                                .app(s.get("app").toString())
                                .uri(s.get("uri").toString())
                                .hits(((Number) s.get("hits")).longValue())
                                .build();
                        statsList.add(viewStats);
                    }
                }
            }


            events.forEach(e -> {
                String s = "/events/" + e.getId();

                List<ViewStatsDto> v = statsList.stream()
                        .filter(o -> o.getUri().equals(s))
                        .limit(1L)
                        .toList();
                //.map(Optional::get);

                if (!v.isEmpty()) {
                    e.setViews(v.getFirst().getHits()); //обновили счётчик
                }

            });
            System.out.println("!!! 9 LocalDateTime.now(): " + LocalDateTime.now());
        }

        //обновим Long confirmedRequests (количество одобренных заявок на участие в данном событии)
        if (getConfirms) {
            //обновим Long confirmedRequests (количество одобренных заявок на участие в данном событии)
            List<Long> idEventList = events.stream().
                    map(Event::getId)
                    .toList();
            System.out.println("!!! 10 LocalDateTime.now(): " + LocalDateTime.now());
            List<Request> confirmsList = requestRepository.findAllByEventIdInAndStatusIs(idEventList, StatusRequest.CONFIRMED);
            System.out.println("!!! 11 LocalDateTime.now(): " + LocalDateTime.now());

            events.forEach(e -> {

                long c = confirmsList.stream()
                        .filter(o -> o.getEvent().getId().equals(e.getId()))
                        .count();  //? - .count(); collect(Collectors.counting())

                if (c != 0) {
                    e.setConfirmedRequests(c); //обновили счётчик
                }

            });

        }

        /*
        ///для каждого Event обновим views (кол-во просмотрев события) и confirmedRequests(кол-во одобренных заявок)
        events.forEach(e -> {
            String s = "/events/" + e.getId();

            List<ViewStatsDto> v = statsList.stream()
                    .filter(o -> o.getUri().equals(s))
                    .limit(1L)
                    .toList();
                    //.map(Optional::get);

            if (!v.isEmpty()) {
                e.setViews(v.getFirst().getHits()); //обновили счётчик
            }

            long c = confirmsList.stream()
                    .filter(o -> o.getEvent().getId().equals(e.getId()))
                    .count();  //? - .count(); collect(Collectors.counting())

            if (c != 0) {
                e.setConfirmedRequests(c); //обновили счётчик
            }


        });


         */
        System.out.println("!!! 12 LocalDateTime.now(): " + LocalDateTime.now());
        return events;
    }




    /*
    @Autowired //аналог включенного @RequiredArgsConstructor
    public TestServiceImpl(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @Override //@Transactional
    public TestDto anyTest(TestDto testDto) {
        Test test = TestMapper.toTest(testDto);
        return TestMapper.toTestDto(testRepository.save(test));
    }

    */

    //    @Override //@Transactional если @Transactional(readOnly = true) активен для класса




}

/*ПРИМЕРЫ

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepositoryImpl.save(user)); //save
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {
        userDto.setId(userId);
        User user = UserMapper.toUser(userDto);

        if (!user.getId().equals(userId)) {
            throw new NotFoundException("Пользователь отличается");
        }

        if (this.findById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        User userTemp = UserMapper.toUser(this.findById(userId));

        if (user.getName() != null) {
            userTemp.setName(user.getName());
        }
        if (user.getEmail() != null) {
            userTemp.setEmail(user.getEmail());
        }

        return UserMapper.toUserDto(userRepositoryImpl.save(userTemp));
    }

    @Override
    public UserDto findById(Long userId) {
        return UserMapper.toUserDto(userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден")));
    }

    @Override
    public List<UserDto> findUsersAll() {
        return userRepositoryImpl.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User userTemp = UserMapper.toUser(this.findById(userId));
        userRepositoryImpl.delete(userTemp);
    }



    @Override
    public ItemRequestResponseDto create(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requestor);

        return ItemRequestMapper.toItemRequestResponseDto(itemRequestRepositoryImpl.save(itemRequest));
    }

    @Override
    public List<ItemRequestResponseDto> findItemRequestAll(Long userId) {
        //проверка существования пользователя
        User requestor = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));

        //вместе с данными об ответах на него
        List<ItemRequestResponseDto> res = itemRequestRepositoryImpl.findAllByRequestorOrderByCreatedDesc(requestor).stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());

        res.forEach(x -> x.setItems(this.findAnswersItemRequest(x.getId())));

        return res;
    }

    @Override
    public List<ItemRequestResponseDto> findItemRequestOtherAll(Long userId) {

        //проверка существования пользователя
        User requestor = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));

        return itemRequestRepositoryImpl.findAllByRequestorNotOrderByCreatedDesc(requestor).stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto findItemRequest(Long userId, Long requestId) {
        //проверка существования пользователя
        User requestor = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));

        ItemRequest itemRequest = itemRequestRepositoryImpl.findAllById(requestId);
        //вместе с данными об ответах на него
        itemRequest.setItems(this.findAnswersItemRequest(itemRequest.getId()));

        return ItemRequestMapper.toItemRequestResponseDto(itemRequest);
    }

    public List<ItemShortDto> findAnswersItemRequest(Long requestId) {
        return itemRepositoryImpl.findAllByRequest(requestId).stream()
                .map(ItemMapper::toItemShortDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        User user = UserMapper.toUser(userServiceImpl.findById(userId));
        item.setOwner(user);

        if (itemDto.getRequestId() != null) {
        itemRequestRepositoryImpl.findById(itemDto.getRequestId())
        .orElseThrow(() -> new NotFoundException("ItemRequest не найден"));
        }
        //System.out.println("!!! itemDto=" + itemDto);
        //System.out.println("!!! item=" + item);

        return ItemMapper.toItemDto(itemRepositoryImpl.save(item));
        }

@Override
@Transactional
public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {

    User user = UserMapper.toUser(userServiceImpl.findById(userId));
    Item item = ItemMapper.toItem(this.findById(itemId, userId));

    if (!item.getOwner().getId().equals(userId)) {
        throw new NotFoundException("Пользователь не владелец вещи");
    }

    if (itemDto.getName() != null) {
        item.setName(itemDto.getName());
    }

    if (itemDto.getDescription() != null) {
        item.setDescription(itemDto.getDescription());
    }

    if (itemDto.getAvailable() != null) {
        item.setAvailable(itemDto.getAvailable());
    }

    return ItemMapper.toItemDto(itemRepositoryImpl.save(item));
}

@Override
public ItemDto findById(Long itemId, Long userId) {
    Item item = itemRepositoryImpl.findById(itemId).orElseThrow(() -> new NotFoundException("Item не найден"));
    if (item.getOwner().getId().equals(userId)) {
        this.lastDateBooking(item);
        this.nextDateBooking(item);
    }

    this.getComments(item);

    return ItemMapper.toItemDto(itemRepositoryImpl.findById(itemId).orElseThrow(() -> new NotFoundException("Item не найден")));
}

public void lastDateBooking(Item item) {
    item.setLastBooking(bookingRepositoryImpl.findFirst1ByItemAndStartBeforeOrderByStartDesc(item, LocalDateTime.now()));
}

public void nextDateBooking(Item item) {
    item.setLastBooking(bookingRepositoryImpl.findFirst1ByItemAndStartAfterOrderByStartAsc(item, LocalDateTime.now()));
}


public void getComments(Item item) {

    item.setComments(commentRepositoryImpl.findAllByItem(item).stream()
            .map(CommentMapper::toCommentDto)
            .collect(Collectors.toList()));

}

@Override
public List<ItemDto> findItemsAll(Long userId) {
    return itemRepositoryImpl.findAllByOwnerId(userId).stream()
            .map(i -> this.findById(i.getId(), userId))
            .collect(Collectors.toList());
}

@Override
public List<ItemDto> searchByText(String text) {
    if (text.isBlank()) {
        return List.of();
    }
    return itemRepositoryImpl.searchByText(text).stream()
            .map(ItemMapper::toItemDto)
            .collect(Collectors.toList());
}

@Transactional
public CommentDto createComment(Long userId,
                                Long itemId,
                                CommentDto commentDto) {

    Comment comment = CommentMapper.toComment(commentDto);

    User user = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));
    Item item = itemRepositoryImpl.findById(itemId).orElseThrow(() -> new NotFoundException("Item не найден"));

    if (bookingRepositoryImpl.findAllByBookerAndEndIsBeforeAndItemAndStatusEquals(user, LocalDateTime.now(),
            item, Status.APPROVED).isEmpty()) {
        throw new BusinessException("Пользователь должен оформить бронирование");
    }

    comment.setItem(item);
    comment.setAuthor(user);

    return CommentMapper.toCommentDto(commentRepositoryImpl.save(comment));
}


    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingDto bookingDto) {
        User booker = UserMapper.toUser(userServiceImpl.findById(userId));

        bookingDto.setStatus(Status.WAITING);

        if (bookingDto.getItemId() == null) {
            throw new NotFoundException("Item не найден");
        }
        Item item = itemRepositoryImpl.findById(bookingDto.getItemId()).orElseThrow(() -> new NotFoundException("Item не найден"));

        if (!item.getAvailable()) {
            throw new BusinessException("Item забронирована");
        }
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);

        return BookingMapper.toBookingResponseDto(bookingRepositoryImpl.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto updateApproved(Long bookingId,
                                     Long userId,
                                     Boolean isApproved) {
        Booking booking = this.findByIdWithoutDto(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessWrongException("Пользователь не владелец вещи");
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BusinessException("Статус уже установлен");
        }

        if (isApproved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        return BookingMapper.toBookingResponseDto(bookingRepositoryImpl.save(booking));
    }

    @Override
    public BookingResponseDto findBooking(Long bookingId, Long userId) {
        Booking booking = this.findByIdWithoutDto(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId) &&
                !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не имеет доступа к чтению данных");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> findBookingAll(Long userId, State state) {

        User booker = UserMapper.toUser(userServiceImpl.findById(userId));

        List<Booking> tempBooking = new ArrayList<>();
        switch (state) {
            case ALL:
                tempBooking = bookingRepositoryImpl.findAllByBookerId(userId, Sort.by(Sort.Direction.DESC, "start"));
                break;
            case CURRENT:
                tempBooking = bookingRepositoryImpl.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                tempBooking = bookingRepositoryImpl.findAllByBookerAndStartAfterAndEndAfterOrderByStartDesc(booker, LocalDateTime.now(), LocalDateTime.now());
                break;
            case FUTURE:
                tempBooking = bookingRepositoryImpl.findJpqlQueryFuture(userId, LocalDateTime.now());
                break;
            case WAITING:
                tempBooking = bookingRepositoryImpl.findJpqlQueryStatus(userId, LocalDateTime.now(), Status.WAITING);
                break;
            case REJECTED:
                tempBooking = bookingRepositoryImpl.findJpqlQueryStatus(userId, LocalDateTime.now(), Status.REJECTED);
                break;
            default:
                throw new BadStateException("Нет такого статуса");
        }

        return tempBooking.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());

    }

    @Override
    public List<BookingDto> findBookingOwnerAll(Long userId, State state) {

        User booker = UserMapper.toUser(userServiceImpl.findById(userId));

        List<Booking> tempBooking = new ArrayList<>();
        switch (state) {
            case ALL:
                tempBooking = bookingRepositoryImpl.findAllByItemOwnerId(userId, Sort.by(Sort.Direction.DESC, "start"));
                break;
            case CURRENT:
                tempBooking = bookingRepositoryImpl.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                tempBooking = bookingRepositoryImpl.findAllByItemOwnerAndStartAfterAndEndAfterOrderByStartDesc(booker, LocalDateTime.now(), LocalDateTime.now());
                break;
            case FUTURE:
                tempBooking = bookingRepositoryImpl.findJpqlQueryFutureOwner(userId, LocalDateTime.now());
                break;
            case WAITING:
                tempBooking = bookingRepositoryImpl.findJpqlQueryStatusOwner(userId, LocalDateTime.now(), Status.WAITING);
                break;
            case REJECTED:
                tempBooking = bookingRepositoryImpl.findJpqlQueryStatusOwner(userId, LocalDateTime.now(), Status.REJECTED);
                break;
            default:
                throw new BadStateException("Нет такого статуса");
        }

        return tempBooking.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto findById(Long bookingId) {
        return BookingMapper.toBookingDto(bookingRepositoryImpl.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking не найден")));
    }

    @Override
    public Booking findByIdWithoutDto(Long bookingId) {
        return bookingRepositoryImpl.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking не найден"));
    }

 */