package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.enums.StateAction;
import ru.practicum.ewm.event.model.Location;

import java.time.LocalDateTime;

@Data //Lombok, чтобы сгенерировать геттеры и сеттеры для полей
@Builder //создаёт через билдер произвольный конструктор
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20,max = 2000) //!!!
    private String annotation;

    private Long category;

    @Size(min=20, max = 7000) //!!!
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate; //Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    private Location location; //Широта и долгота места проведения события
    private Boolean paid; //Нужно ли оплачивать участие
    @PositiveOrZero
    private Integer participantLimit; //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения
    private Boolean requestModeration; //Нужна ли пре-модерация заявок на участие
    private StateAction stateAction; //новое состояний жизненного цикла события

    @Size(min = 3,max = 120) //!!!
    private String title; //Заголовок
}

//@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")