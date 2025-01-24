package ru.practicum.ewm.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@Entity //POM <artifactId>jakarta.persistence-api</artifactId>
@Table(name = "users", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 254, unique = true)
    private String email;

    @Column(name = "name", nullable = false, length = 250)
    private String name;


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
