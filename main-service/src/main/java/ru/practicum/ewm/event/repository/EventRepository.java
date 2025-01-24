package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    List<Event> findAllByIdIn(List<Long> events);

    List<Event> findAllByInitiator(User Initiator, PageRequest pagenation);

    Optional<Event> findAllByInitiatorAndId(User Initiator, Long id);

    Boolean existsByCategoryId(Long catId);
}
