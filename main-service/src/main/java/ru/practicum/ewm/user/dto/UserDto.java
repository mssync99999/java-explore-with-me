package ru.practicum.ewm.user.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //Lombok, чтобы сгенерировать геттеры и сеттеры для полей
@Builder //создаёт через билдер произвольный конструктор
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    //@NotNull readOnly: true
    private Long id;

    @NotBlank
    private String email;

    @NotBlank //@Size(min = 1,max = 250)
    private String name;



}
