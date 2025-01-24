package ru.practicum.ewm.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;
import java.util.List;

//API для работы с пользователями
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateRequestController {

    private final RequestService requestService;

    //Получение информации о заявках текущего пользователя на участие в чужих событиях
    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {

        log.info("/users/{userId}/requests PrivateRequestController.getUserRequests(userId {})", userId);
        return requestService.getUserRequests(userId);

    }

    //Добавление запроса от текущего пользователя на участие в событии
    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED) //201
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                           @RequestParam @Valid Long eventId) {

        log.info("/users/{userId}/requests PrivateRequestController.addParticipationRequest(userId {}, eventId {})", userId, eventId);
        return requestService.addParticipationRequest(userId, eventId);

    }

    //Отмена своего запроса на участие в событии
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK) //200
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("/users/{userId}/requests/{requestId}/cancel PrivateRequestController.cancelRequest(userId {}, requestId {}):", userId, requestId);
        return requestService.cancelRequest(userId, requestId);

    }

}