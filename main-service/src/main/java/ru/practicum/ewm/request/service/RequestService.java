package ru.practicum.ewm.request.service;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface RequestService {

    //Получение информации о заявках текущего пользователя на участие в чужих событиях
    List<ParticipationRequestDto> getUserRequests(Long userId);

    //Добавление запроса от текущего пользователя на участие в событии
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    //Отмена своего запроса на участие в событии
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

}
