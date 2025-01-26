package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Location;
import java.time.LocalDateTime;

@Data //Lombok, чтобы сгенерировать геттеры и сеттеры для полей
@Builder //создаёт через билдер произвольный конструктор
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    @NotBlank
    @Size(min = 20,max = 2000)
    private String annotation; //Краткое описание события

    @NotNull
    private Long category; //id категории к которой относится событие

    @NotBlank
    @Size(min = 20,max = 7000)
    private String description; //Полное описание события

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate; //Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    @NotNull
    private Location location; //Широта и долгота места проведения события

    @Builder.Default
    private Boolean paid = false; //Нужно ли оплачивать участие

    @Builder.Default
    @PositiveOrZero
    private Integer participantLimit = 0; //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    @Builder.Default
    private Boolean requestModeration = true; //Нужна ли пре-модерация заявок на участие

    @NotBlank
    @Size(min = 3,max = 120)
    private String title; //Заголовок

}
