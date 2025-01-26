package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.enums.StatusRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;


    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));


        List<ParticipationRequestDto> requestList = requestRepository.findAllByRequester(user).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());

        return requestList;

    }

    @Transactional
    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Optional<Request> requestOptional = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (requestOptional.isPresent()) {
            throw new DoubleException("Нельзя добавить повторный запрос");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new BadInitiatorException("Инициатор события не может добавить запрос на участие в своём событии");

        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new BadStateException("Нельзя участвовать в неопубликованном событии");
        }

        if (!event.getParticipantLimit().equals(0)) {

            if (event.getConfirmedRequests().equals(event.getParticipantLimit().longValue())) {
                throw new BadLimitException("Событие достигло лимит запросов на участие");
            }

        }


        Request newObj = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(StatusRequest.PENDING)
                .build();

        //!!!если если для события лимит заявок равен 0!!! или для события отключена пре-модерация запросов на участие,
        // то запрос должен автоматически перейти в состояние подтвержденного
        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0)) {
            newObj.setStatus(StatusRequest.CONFIRMED);
            Long tmp = event.getConfirmedRequests() + 1;
            event.setConfirmedRequests(tmp); //инкримент счётчика
        }

        return RequestMapper.toParticipationRequestDto(requestRepository.save(newObj)); //нельзя добавить повторный запрос (Ожидается код ошибки 409)
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Request request = requestRepository.findByRequesterIdAndId(userId, requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден или недоступен"));

        //наеобходим декремент счётчика подтверждений в связанном Event
        //но только при наличия подтверждение на участие
        if (request.getStatus() == StatusRequest.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        }
        request.setStatus(StatusRequest.CANCELED);

        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

}