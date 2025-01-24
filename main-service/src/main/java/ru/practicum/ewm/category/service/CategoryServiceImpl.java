package ru.practicum.ewm.category.service;

//import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
//import ru.practicum.ewm.category.mapper.TestMapper;
import ru.practicum.ewm.category.model.Category;
//import ru.practicum.ewm.category.model.Test;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.user.model.User;

import java.util.List;

//@Transactional(readOnly = true)
 //@NoArgsConstructor @AllArgsConstructor
//@NoArgsConstructor
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    /*
    @Autowired //аналог включенного @RequiredArgsConstructor
    public TestServiceImpl(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @Override //@Transactional
    public TestDto anyTest(TestDto testDto) {
        Test test = TestMapper.toTest(testDto);
        return TestMapper.toTestDto(testRepository.save(test));
    }

    */

    @Override //@Transactional если @Transactional(readOnly = true) активен для класса
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategory(newCategoryDto);
        //базовые проверки
        if (categoryRepository.existsByIdNotAndNameIs(0L, category.getName())) {
            //? заменить на более правильный чекинг existsByIdNeAndName(Long catId, String name)
            System.out.println("!!! categoryRepository.existsByIdNotAndNameIs");
            throw new DoubleException("Нарушение целостности данных53"); //409
        }

        return CategoryMapper.toCategoryDto(categoryRepository.save(category)); //ождиаем непроверяемое исключение 409 уникальность
    }

    @Override //@Transactional если @Transactional(readOnly = true) активен для класса
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            //? заменить на более правильный чекинг
            throw new NotFoundException("Категория не найдена или недоступна"); //404
        }
        /**/
        //включим когда создадим Event
        if (eventRepository.existsByCategoryId(catId)) {
            throw new WrongConditionsException("Существуют события, связанные с категорией");
        }

        categoryRepository.deleteById(catId);
    }

    @Override //@Transactional если @Transactional(readOnly = true) активен для класса
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        /*
        if (!categoryRepository.existsById(catId)) {
            //? заменить на более правильный чекинг
            throw new NotFoundException("Категория не найдена или недоступна"); //404
        }

         */
        //базовые проверки
        if (categoryRepository.existsByIdNotAndNameIs(catId, categoryDto.getName())) {
            //? заменить на более правильный чекинг existsByIdNeAndName(Long catId, String name)
            System.out.println("!!! categoryRepository.existsByIdNotAndNameIs");
            throw new DoubleException("Нарушение целостности данных79"); //409
        }

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        //Category category = CategoryMapper.toCategory(categoryDto);
        category.setName(categoryDto.getName());
/*

*/

        return CategoryMapper.toCategoryDto(/*result*/ categoryRepository.save(category)); //ожидаем непроверяемое исключение 409 уникальность


    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        PageRequest pagenation = PageRequest.of(from / size, size);

        return categoryRepository.findAll(pagenation).stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();

    }

    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена или недоступна"));
        return CategoryMapper.toCategoryDto(category);

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