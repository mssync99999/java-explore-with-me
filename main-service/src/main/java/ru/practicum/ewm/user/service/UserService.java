package ru.practicum.ewm.user.service;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import java.util.List;

public interface UserService {

    //Получение информации о пользователях
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    //Добавление нового пользователя
    UserDto registerUser(@RequestBody @Valid NewUserRequest newUserRequest);

    //Удаление пользователя
    void deleteUser(Long userId);

}
