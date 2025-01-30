package ru.practicum.ewm.comment.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.enums.StatusComment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "comments", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private StatusComment status; //Список состояний жизненного цикла комментария

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event; //Событие

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; //Пользователь

    private LocalDateTime created; //Дата и время создания комментария (в формате "yyyy-MM-dd HH:mm:ss")

}