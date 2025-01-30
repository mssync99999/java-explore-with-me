package ru.practicum.ewm.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.enums.StatusComment;
import java.time.LocalDateTime;

@Data //Lombok, чтобы сгенерировать геттеры и сеттеры для полей
@Builder //создаёт через билдер произвольный конструктор
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;

    @NotBlank
    @Size(min = 1,max = 512)
    private String content;

    @NotBlank
    @Size(min = 1,max = 50)
    private StatusComment status; //Список состояний жизненного цикла комментария

    @NotNull
    private Long eventId; //Событие

    @NotNull
    private Long creatorId; //Пользователь

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created; //Дата и время создания комментария (в формате "yyyy-MM-dd HH:mm:ss")

}
