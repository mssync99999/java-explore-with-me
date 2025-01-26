package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.enums.StatusRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.model.User;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByRequester(User user);

    Optional<Request> findByRequesterIdAndId(Long userId, Long requestId);

    List<Request> findAllByIdIn(List<Long> ids);

    List<Request> findAllByEventIdAndStatusIs(Long userId, StatusRequest status);

    List<Request> findAllByEventIdInAndStatusIs(List<Long> idEventList, StatusRequest status);

    List<Request> findAllByEventInitiatorAndEvent(User user, Event event);

}
