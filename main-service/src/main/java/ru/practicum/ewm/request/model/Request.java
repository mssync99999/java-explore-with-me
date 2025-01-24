package ru.practicum.ewm.request.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.enums.StatusRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@Entity //POM <artifactId>jakarta.persistence-api</artifactId>
@Table(name = "requests", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne //позволяет делать запросные методы с id , например - findAllByBookerId наряду с findAllByBooker
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne //позволяет делать запросные методы с id , например - findAllByBookerId наряду с findAllByBooker
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    private LocalDateTime created; //Дата и время создания события (в формате "yyyy-MM-dd HH:mm:ss")

    @Enumerated(EnumType.STRING) //@Column(length = 25)  //?-@Size
    private StatusRequest status; //Список состояний жизненного цикла события


    //@ManyToOne //позволяет делать запросные методы с id , например - findAllByBookerId наряду с findAllByBooker
    //@JoinColumn(name = "booker_id")
    //private User booker;

    //@Enumerated(EnumType.STRING)
    //@Column(name = "status", nullable = false)
    //private Status status;

    //@Transient
    //private Booking nextBooking;
    //@Transient
    //private List<CommentDto> comments;
}
