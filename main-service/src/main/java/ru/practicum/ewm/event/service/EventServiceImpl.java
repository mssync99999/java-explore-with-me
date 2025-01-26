package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;

    @Value("${ewm.service.name}")
    private String serviceName;

    @Value("${stats-server.url}")
    private String statsServerUrl;

    @Override
    public List<EventShortDto> getEvents_1(String text, List<Long> categories, Boolean paid,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Boolean onlyAvailable, SortType sort,
                                           Integer from, Integer size, HttpServletRequest request) {

        //базовые проверки
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Дата окончания rangeEnd раньше даты начала события rangeStart.");
        }

        //определяем сортировку и пагинацию
        Sort sortAsc = switch (sort) {
            case SortType.EVENT_DATE -> Sort.by(Sort.Direction.DESC, "eventDate");
            case SortType.VIEWS -> Sort.by("views");
        };

        PageRequest pagenation = PageRequest.of(from / size, size, sortAsc);

        //подготовка и получение данных из репозитория через Query DSL
        BooleanExpression conditions = QEvent.event.state.in(State.PUBLISHED); //инициализируем стартовым условием

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

        StatsClient statsClient = new StatsClient(this.statsServerUrl, new RestTemplateBuilder());

        try {
            EndpointHitDto endpointHitDto = EndpointHitDto
                    .builder()
                    .app(this.serviceName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.create(endpointHitDto);

        } catch (BadStatServiceException e) {
            e.getMessage();
        }

        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventFullDto> getEvents_2(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        Sort sortCreatedOnAsc = Sort.by(Sort.Direction.DESC, "createdOn"); //сортируем от старых к новым при извлечении

        PageRequest pagenation = PageRequest.of(from / size, size, sortCreatedOnAsc);

        //базовые проверки
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Дата окончания rangeEnd раньше даты начала события rangeStart.");
        }

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

        List<Event> eventsList = eventRepository.findAll(conditions, pagenation).getContent();

        List<Event> events = getRefreshViewsAndConfirms(eventsList, true, false);

        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());

    }

    @Transactional
    @Override
    public EventFullDto updateEvent_1(Long eventId, UpdateEventAdminRequest obj) {

        //базовые проверки
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));


        //дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
        if (obj.getEventDate() != null) {
            if (!obj.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила.");

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
                throw new WrongConditionsException("Событие можно отклонить, только если оно еще не опубликовано");
            }
        }

        //обновление изменяемых полей
        if (obj.getAnnotation() != null && !obj.getAnnotation().isBlank()) {
            event.setAnnotation(obj.getAnnotation());
        }
        if (obj.getCategory() != null) {
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
            locationRepository.save(obj.getLocation());
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

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getEvents(Long userId, Integer from, Integer size) {
        return this.getEvents_2(List.of(userId), null, null, LocalDateTime.now().minusYears(100L), LocalDateTime.now().plusYears(100L), from, size);
    }

    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        //Обратите внимание: дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента

        if (!newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
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

        return EventMapper.toEventFullDto(eventRepository.findAllByInitiatorAndId(user, eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found")));
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest obj) {

        //базовые проверки
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != State.CANCELED && event.getState() != State.PENDING) {
            throw new WrongConditionsException("Only pending or canceled events can be changed");

        }

        if (obj.getEventDate() != null) {
            if (!obj.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила.");
            }
        }

        //обновление изменяемых полей
        if (obj.getAnnotation() != null && !obj.getAnnotation().isBlank()) {
            event.setAnnotation(obj.getAnnotation());
        }
        if (obj.getCategory() != null) {

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

        List<ParticipationRequestDto> requestList = requestRepository.findAllByEventInitiatorAndEvent(user, event).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());

        return requestList;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest obj) {

        //базовые проверки
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (obj.getStatus() != Status.CONFIRMED && obj.getStatus() != Status.REJECTED) {
            throw new BadRequestException("Поступил некорректный статус" + obj.getStatus());

        }

        boolean newStatusConfirmed;
        if (obj.getStatus() == Status.CONFIRMED) {
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
                eventRepository.save(event);
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

        //базовые проверки
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Посмотреть можно только опубликованное событие.");
        }

        Event result = getRefreshViewsAndConfirms(List.of(event), true, true).getFirst();

        StatsClient statsClient = new StatsClient(this.statsServerUrl, new RestTemplateBuilder());
        try {
            EndpointHitDto endpointHitDto = EndpointHitDto
                    .builder()
                    .app(this.serviceName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.create(endpointHitDto);

        } catch (BadStatServiceException e) {
            e.getMessage();
        }

        return EventMapper.toEventFullDto(result);
    }


    public List<Event> getRefreshViewsAndConfirms(List<Event> events, Boolean getViews, Boolean getConfirms) {

        if (events.isEmpty()) {
            return null;
        }

        //обновим Long views (кол-во просмотрев события) Long views
        if (getViews) {


            //обновим Long views (кол-во просмотрев события) Long views
            List<String> urisEventId = events
                    .stream()
                    .map(event -> "/events/" + event.getId())
                    .toList();


            StatsClient statsClient = new StatsClient(this.statsServerUrl, new RestTemplateBuilder());


            Object responseBody = statsClient.getStats(events.getFirst().getCreatedOn().minusDays(1),
                    LocalDateTime.now(), urisEventId, true).getBody();


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


                if (!v.isEmpty()) {
                    e.setViews(v.getFirst().getHits()); //обновили счётчик
                }

            });

        }

        //обновим Long confirmedRequests (количество одобренных заявок на участие в данном событии)
        if (getConfirms) {
            //обновим Long confirmedRequests (количество одобренных заявок на участие в данном событии)
            List<Long> idEventList = events.stream()
                    .map(Event::getId)
                    .toList();

            List<Request> confirmsList = requestRepository.findAllByEventIdInAndStatusIs(idEventList, StatusRequest.CONFIRMED);


            events.forEach(e -> {

                long c = confirmsList.stream()
                        .filter(o -> o.getEvent().getId().equals(e.getId()))
                        .count();

                if (c != 0) {
                    e.setConfirmedRequests(c); //обновили счётчик
                }

            });

        }

        return events;
    }

}