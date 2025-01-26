package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

//данные для обновления подборки
@Data //Lombok, чтобы сгенерировать геттеры и сеттеры для полей
@Builder //создаёт через билдер произвольный конструктор
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationRequest {
    private List<Long> events; //Список идентификаторов событий входящих в подборку ?-Set<Long>
    private Boolean pinned; //Закреплена ли подборка на главной странице сайта

    @Size(min = 1,max = 50)
    private String title; //Заголовок подборки
}
