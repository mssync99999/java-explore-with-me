package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.enums.StateAction;
import ru.practicum.ewm.enums.Status;
import ru.practicum.ewm.event.model.Location;

import java.time.LocalDateTime;
import java.util.List;


//Изменение статуса запроса на участие в событии текущего пользователя
@Data //Lombok, чтобы сгенерировать геттеры и сеттеры для полей
@Builder //создаёт через билдер произвольный конструктор
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds; //Идентификаторы запросов на участие в событии текущего пользователя
    private Status status; //Новый статус запроса на участие в событии тек. пользователя CONFIRMED или REJECTED

}

//@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")