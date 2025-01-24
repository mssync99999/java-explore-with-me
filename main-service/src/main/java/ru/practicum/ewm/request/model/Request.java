package ru.practicum.ewm.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.enums.StatusRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "requests", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    private LocalDateTime created; //Дата и время создания события (в формате "yyyy-MM-dd HH:mm:ss")

    @Enumerated(EnumType.STRING)
    private StatusRequest status; //Список состояний жизненного цикла события

}
