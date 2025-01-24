package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import java.util.List;

public interface RequestService {

    //Получение информации о заявках текущего пользователя на участие в чужих событиях
    List<ParticipationRequestDto> getUserRequests(Long userId);

    //Добавление запроса от текущего пользователя на участие в событии
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    //Отмена своего запроса на участие в событии
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

}
