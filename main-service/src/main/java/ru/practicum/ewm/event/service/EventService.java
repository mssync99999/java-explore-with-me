package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.enums.SortType;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    //Поиск событий
    List<EventFullDto> getEvents_2(List<Long> users,
                                          List<State> states,
                                          List<Long> categories,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          Integer from,
                                          Integer size);

    //Изменение события
    EventFullDto updateEvent_1(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    //Получение событий, добавленных текущим пользователем
    List<EventFullDto> getEvents(Long userId, Integer from, Integer size);

    //Добавление нового события
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    //Получение полной информации о событии добавленном текущим пользователем
    EventFullDto getEvent(Long userId, Long eventId);

    //Изменение события добавленного текущим пользователем
    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    //Получение информации о запросах на участие в событии текущего пользователя
    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    //Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest obj);

    //Получение событий с возможностью фильтрации
    List<EventShortDto> getEvents_1(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                    Boolean onlyAvailable, SortType sort, Integer from, Integer size, HttpServletRequest request);

    //Получение подборки событий по его id
    EventFullDto getEvent_1(Long id, HttpServletRequest request);

}
