package ru.practicum.ewm.event.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@Entity //POM <artifactId>jakarta.persistence-api</artifactId>
@Table(name = "locations")
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    Float lat;
    Float lon;

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
