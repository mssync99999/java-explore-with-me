package ru.practicum.ewm.category.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "categories")
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Обратите внимание: имя категории должно быть уникальным
    @Column(nullable = false, length = 50, unique = true)
    private String name;

}
