package ru.practicum.ewm.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.event.model.Location;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@Entity //POM <artifactId>jakarta.persistence-api</artifactId>
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @ManyToOne //позволяет делать запросные методы с id , например - findAllByBookerId наряду с findAllByBooker
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private Long confirmedRequests; //Количество одобренных заявок на участие в данном событии

    private LocalDateTime createdOn; //Дата и время создания события (в формате "yyyy-MM-dd HH:mm:ss")

    @Column(length = 7000) //?-@Size
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate; //Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")

    @ManyToOne //позволяет делать запросные методы с id , например - findAllByBookerId наряду с findAllByBooker
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator; //Пользователь (краткая информация)

    @ManyToOne //позволяет делать запросные методы с id , например - findAllByBookerId наряду с findAllByBooker
    @JoinColumn(name = "location_id") //, nullable = false)
    private Location location; //Широта и долгота места проведения события


    @Column(nullable = false)  //?-@NotNull
    private Boolean paid; //Нужно ли оплачивать участие

    @Column(name = "participant_limit")
    private Integer participantLimit; //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    @Column(name = "published_on")
    private LocalDateTime publishedOn; //Дата и время публикации события (в формате "yyyy-MM-dd HH:mm:ss")

    @Column(name = "request_moderation") //default: true
    private Boolean requestModeration; //Нужна ли пре-модерация заявок на участие

    @Enumerated(EnumType.STRING)
    @Column(length = 25)  //?-@Size
    private State state; //Список состояний жизненного цикла события

    @Column(nullable = false, length = 2000)
    private String title; //Заголовок

    private Long views; //Количество просмотрев события


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
