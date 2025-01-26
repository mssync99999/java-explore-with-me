package ru.practicum.ewm.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;
import java.util.List;

//API для работы с пользователями
@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    //Получение информации о пользователях
    @GetMapping
    public List<UserDto> getUsers(@RequestParam(defaultValue = "") List<Long> ids,
                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                  @Positive @RequestParam(defaultValue = "10") Integer size) {

        log.info("AdminUserController.getUsers(ids {}, from {}, size {})", ids, from, size);
        return userService.getUsers(ids, from, size);

    }


    //Добавление нового пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //201
    public UserDto registerUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("AdminUserController.registerUser(newUserRequest {})", newUserRequest);
        return userService.registerUser(newUserRequest);

    }

    //Удаление пользователя
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteUser(@PathVariable Long userId) {
        log.info("AdminCompilationController.delete(compId {}):", userId);
        //return null;
        userService.deleteUser(userId);

    }


}
