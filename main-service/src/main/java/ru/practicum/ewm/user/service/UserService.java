package ru.practicum.ewm.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.enums.SortType;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService {

    //Получение информации о пользователях
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    //Добавление нового пользователя
    UserDto registerUser(@RequestBody @Valid NewUserRequest newUserRequest);

    //Удаление пользователя
    void deleteUser(Long userId);

}
