package ru.practicum.ewm.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.event.model.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@Entity //POM <artifactId>jakarta.persistence-api</artifactId>
@Table(name = "compilations", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean pinned; //Закреплена ли подборка на главной странице сайта
    //Обратите внимание: имя категории должно быть уникальным
    @Column(nullable = false, length = 50) //, unique = true)
    private String title;

    @ManyToMany
    @JoinTable(name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;
}
