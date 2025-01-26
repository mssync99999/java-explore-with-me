package ru.practicum.ewm.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
    public static UserShortDto toUserShortDto(User e) {
        return UserShortDto.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }

    public static UserDto toUserDto(User e) {
        return UserDto.builder()
                .id(e.getId())
                .email(e.getEmail())
                .name(e.getName())
                .build();
    }

    public static NewUserRequest toNewUserRequest(User e) {
        return NewUserRequest.builder()
                .email(e.getEmail())
                .name(e.getName())
                .build();
    }


    public static User toUser(UserShortDto e) {
        return User.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }

    public static User toUser(UserDto e) {
        return User.builder()
                .id(e.getId())
                .email(e.getEmail())
                .name(e.getName())
                .build();
    }

    public static User toUser(NewUserRequest e) {
        return User.builder()
                .email(e.getEmail())
                .name(e.getName())
                .build();
    }

}
