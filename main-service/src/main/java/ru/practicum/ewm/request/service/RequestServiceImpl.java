package ru.practicum.ewm.request.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.enums.Status;
import ru.practicum.ewm.enums.StatusRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//import static ru.practicum.ewm.enums.StatusRequest.PENDING;

@Service
@RequiredArgsConstructor  //активирует "private final TestStorage testStorage",аналог @Autowired, требует org.projectlombok в pom dependacys
//@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;


    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));


        List<ParticipationRequestDto> requestList = requestRepository.findAllByRequester(user).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());

        return requestList;

/*
        return requestRepository.findAllByUserId(userId);
*/

    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        /*
        Обратите внимание:

нельзя добавить повторный запрос (Ожидается код ошибки 409)+
инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)+
нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)+
если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)+
!!!если для события лимит заявок равен 0 или!!! для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного+
         */

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Optional<Request> requestOptional = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (requestOptional.isPresent()) {
            throw new DoubleException("Нельзя добавить повторный запрос");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new BadInitiatorException("Инициатор события не может добавить запрос на участие в своём событии");

        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new BadStateException("Нельзя участвовать в неопубликованном событии");
        }

        //Integer participantLimit = event.getParticipantLimit();
        //Integer confirmedRequests = event.getConfirmedRequests();
        System.out.println("!!! event=" + event);

        if (!event.getParticipantLimit().equals(0)) {
            System.out.println("!!! event.getParticipantLimit()=" + event.getParticipantLimit()+ " event.getConfirmedRequests()");
            if (event.getConfirmedRequests().equals(event.getParticipantLimit().longValue())) {
                throw new BadLimitException("Событие достигло лимит запросов на участие");
            }

        }


/*
        Optional<Double> optionalPrice = Optional.of(123.4);
        System.out.println(optionalPrice.isPresent()); // вернёт true
        System.out.println(optionalPrice.isEmpty()); // вернёт false
 */


        Request newObj = Request.builder()
                //.id(e.getId())
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(StatusRequest.PENDING)
                //...
                //.itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .build();

        //!!!если если для события лимит заявок равен 0!!! или для события отключена пре-модерация запросов на участие,
        // то запрос должен автоматически перейти в состояние подтвержденного
        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0)) {
            newObj.setStatus(StatusRequest.CONFIRMED);
            Long tmp = event.getConfirmedRequests() + 1;
            event.setConfirmedRequests(tmp); //инкримент счётчика
        }

        return RequestMapper.toParticipationRequestDto(requestRepository.save(newObj)); //нельзя добавить повторный запрос (Ожидается код ошибки 409)
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Request request = requestRepository.findByRequesterIdAndId(userId, requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден или недоступен"));

        //наеобходим декремент счётчика подтверждений в связанном Event
        //но только при наличия подтверждение на участие
        if (request.getStatus() == StatusRequest.CONFIRMED) {
            Event event = request.getEvent();

            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        }
        request.setStatus(StatusRequest.CANCELED);

        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

}
/*ПРИМЕРЫ

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepositoryImpl.save(user)); //save
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {
        userDto.setId(userId);
        User user = UserMapper.toUser(userDto);

        if (!user.getId().equals(userId)) {
            throw new NotFoundException("Пользователь отличается");
        }

        if (this.findById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        User userTemp = UserMapper.toUser(this.findById(userId));

        if (user.getName() != null) {
            userTemp.setName(user.getName());
        }
        if (user.getEmail() != null) {
            userTemp.setEmail(user.getEmail());
        }

        return UserMapper.toUserDto(userRepositoryImpl.save(userTemp));
    }

    @Override
    public UserDto findById(Long userId) {
        return UserMapper.toUserDto(userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден")));
    }

    @Override
    public List<UserDto> findUsersAll() {
        return userRepositoryImpl.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User userTemp = UserMapper.toUser(this.findById(userId));
        userRepositoryImpl.delete(userTemp);
    }



    @Override
    public ItemRequestResponseDto create(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requestor);

        return ItemRequestMapper.toItemRequestResponseDto(itemRequestRepositoryImpl.save(itemRequest));
    }

    @Override
    public List<ItemRequestResponseDto> findItemRequestAll(Long userId) {
        //проверка существования пользователя
        User requestor = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));

        //вместе с данными об ответах на него
        List<ItemRequestResponseDto> res = itemRequestRepositoryImpl.findAllByRequestorOrderByCreatedDesc(requestor).stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());

        res.forEach(x -> x.setItems(this.findAnswersItemRequest(x.getId())));

        return res;
    }

    @Override
    public List<ItemRequestResponseDto> findItemRequestOtherAll(Long userId) {

        //проверка существования пользователя
        User requestor = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));

        return itemRequestRepositoryImpl.findAllByRequestorNotOrderByCreatedDesc(requestor).stream()
                .map(ItemRequestMapper::toItemRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto findItemRequest(Long userId, Long requestId) {
        //проверка существования пользователя
        User requestor = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));

        ItemRequest itemRequest = itemRequestRepositoryImpl.findAllById(requestId);
        //вместе с данными об ответах на него
        itemRequest.setItems(this.findAnswersItemRequest(itemRequest.getId()));

        return ItemRequestMapper.toItemRequestResponseDto(itemRequest);
    }

    public List<ItemShortDto> findAnswersItemRequest(Long requestId) {
        return itemRepositoryImpl.findAllByRequest(requestId).stream()
                .map(ItemMapper::toItemShortDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        User user = UserMapper.toUser(userServiceImpl.findById(userId));
        item.setOwner(user);

        if (itemDto.getRequestId() != null) {
        itemRequestRepositoryImpl.findById(itemDto.getRequestId())
        .orElseThrow(() -> new NotFoundException("ItemRequest не найден"));
        }
        //System.out.println("!!! itemDto=" + itemDto);
        //System.out.println("!!! item=" + item);

        return ItemMapper.toItemDto(itemRepositoryImpl.save(item));
        }

@Override
@Transactional
public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {

    User user = UserMapper.toUser(userServiceImpl.findById(userId));
    Item item = ItemMapper.toItem(this.findById(itemId, userId));

    if (!item.getOwner().getId().equals(userId)) {
        throw new NotFoundException("Пользователь не владелец вещи");
    }

    if (itemDto.getName() != null) {
        item.setName(itemDto.getName());
    }

    if (itemDto.getDescription() != null) {
        item.setDescription(itemDto.getDescription());
    }

    if (itemDto.getAvailable() != null) {
        item.setAvailable(itemDto.getAvailable());
    }

    return ItemMapper.toItemDto(itemRepositoryImpl.save(item));
}

@Override
public ItemDto findById(Long itemId, Long userId) {
    Item item = itemRepositoryImpl.findById(itemId).orElseThrow(() -> new NotFoundException("Item не найден"));
    if (item.getOwner().getId().equals(userId)) {
        this.lastDateBooking(item);
        this.nextDateBooking(item);
    }

    this.getComments(item);

    return ItemMapper.toItemDto(itemRepositoryImpl.findById(itemId).orElseThrow(() -> new NotFoundException("Item не найден")));
}

public void lastDateBooking(Item item) {
    item.setLastBooking(bookingRepositoryImpl.findFirst1ByItemAndStartBeforeOrderByStartDesc(item, LocalDateTime.now()));
}

public void nextDateBooking(Item item) {
    item.setLastBooking(bookingRepositoryImpl.findFirst1ByItemAndStartAfterOrderByStartAsc(item, LocalDateTime.now()));
}


public void getComments(Item item) {

    item.setComments(commentRepositoryImpl.findAllByItem(item).stream()
            .map(CommentMapper::toCommentDto)
            .collect(Collectors.toList()));

}

@Override
public List<ItemDto> findItemsAll(Long userId) {
    return itemRepositoryImpl.findAllByOwnerId(userId).stream()
            .map(i -> this.findById(i.getId(), userId))
            .collect(Collectors.toList());
}

@Override
public List<ItemDto> searchByText(String text) {
    if (text.isBlank()) {
        return List.of();
    }
    return itemRepositoryImpl.searchByText(text).stream()
            .map(ItemMapper::toItemDto)
            .collect(Collectors.toList());
}

@Transactional
public CommentDto createComment(Long userId,
                                Long itemId,
                                CommentDto commentDto) {

    Comment comment = CommentMapper.toComment(commentDto);

    User user = userRepositoryImpl.findById(userId).orElseThrow(() -> new NotFoundException("User не найден"));
    Item item = itemRepositoryImpl.findById(itemId).orElseThrow(() -> new NotFoundException("Item не найден"));

    if (bookingRepositoryImpl.findAllByBookerAndEndIsBeforeAndItemAndStatusEquals(user, LocalDateTime.now(),
            item, Status.APPROVED).isEmpty()) {
        throw new BusinessException("Пользователь должен оформить бронирование");
    }

    comment.setItem(item);
    comment.setAuthor(user);

    return CommentMapper.toCommentDto(commentRepositoryImpl.save(comment));
}


    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingDto bookingDto) {
        User booker = UserMapper.toUser(userServiceImpl.findById(userId));

        bookingDto.setStatus(Status.WAITING);

        if (bookingDto.getItemId() == null) {
            throw new NotFoundException("Item не найден");
        }
        Item item = itemRepositoryImpl.findById(bookingDto.getItemId()).orElseThrow(() -> new NotFoundException("Item не найден"));

        if (!item.getAvailable()) {
            throw new BusinessException("Item забронирована");
        }
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);

        return BookingMapper.toBookingResponseDto(bookingRepositoryImpl.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto updateApproved(Long bookingId,
                                     Long userId,
                                     Boolean isApproved) {
        Booking booking = this.findByIdWithoutDto(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessWrongException("Пользователь не владелец вещи");
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BusinessException("Статус уже установлен");
        }

        if (isApproved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        return BookingMapper.toBookingResponseDto(bookingRepositoryImpl.save(booking));
    }

    @Override
    public BookingResponseDto findBooking(Long bookingId, Long userId) {
        Booking booking = this.findByIdWithoutDto(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId) &&
                !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не имеет доступа к чтению данных");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> findBookingAll(Long userId, State state) {

        User booker = UserMapper.toUser(userServiceImpl.findById(userId));

        List<Booking> tempBooking = new ArrayList<>();
        switch (state) {
            case ALL:
                tempBooking = bookingRepositoryImpl.findAllByBookerId(userId, Sort.by(Sort.Direction.DESC, "start"));
                break;
            case CURRENT:
                tempBooking = bookingRepositoryImpl.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                tempBooking = bookingRepositoryImpl.findAllByBookerAndStartAfterAndEndAfterOrderByStartDesc(booker, LocalDateTime.now(), LocalDateTime.now());
                break;
            case FUTURE:
                tempBooking = bookingRepositoryImpl.findJpqlQueryFuture(userId, LocalDateTime.now());
                break;
            case WAITING:
                tempBooking = bookingRepositoryImpl.findJpqlQueryStatus(userId, LocalDateTime.now(), Status.WAITING);
                break;
            case REJECTED:
                tempBooking = bookingRepositoryImpl.findJpqlQueryStatus(userId, LocalDateTime.now(), Status.REJECTED);
                break;
            default:
                throw new BadStateException("Нет такого статуса");
        }

        return tempBooking.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());

    }

    @Override
    public List<BookingDto> findBookingOwnerAll(Long userId, State state) {

        User booker = UserMapper.toUser(userServiceImpl.findById(userId));

        List<Booking> tempBooking = new ArrayList<>();
        switch (state) {
            case ALL:
                tempBooking = bookingRepositoryImpl.findAllByItemOwnerId(userId, Sort.by(Sort.Direction.DESC, "start"));
                break;
            case CURRENT:
                tempBooking = bookingRepositoryImpl.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                tempBooking = bookingRepositoryImpl.findAllByItemOwnerAndStartAfterAndEndAfterOrderByStartDesc(booker, LocalDateTime.now(), LocalDateTime.now());
                break;
            case FUTURE:
                tempBooking = bookingRepositoryImpl.findJpqlQueryFutureOwner(userId, LocalDateTime.now());
                break;
            case WAITING:
                tempBooking = bookingRepositoryImpl.findJpqlQueryStatusOwner(userId, LocalDateTime.now(), Status.WAITING);
                break;
            case REJECTED:
                tempBooking = bookingRepositoryImpl.findJpqlQueryStatusOwner(userId, LocalDateTime.now(), Status.REJECTED);
                break;
            default:
                throw new BadStateException("Нет такого статуса");
        }

        return tempBooking.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto findById(Long bookingId) {
        return BookingMapper.toBookingDto(bookingRepositoryImpl.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking не найден")));
    }

    @Override
    public Booking findByIdWithoutDto(Long bookingId) {
        return bookingRepositoryImpl.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking не найден"));
    }

 */