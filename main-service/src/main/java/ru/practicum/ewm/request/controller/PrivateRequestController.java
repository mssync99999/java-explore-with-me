package ru.practicum.ewm.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;

//API для работы с пользователями
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor //@Validated
public class PrivateRequestController {

    private final RequestService requestService;


    //Получение информации о заявках текущего пользователя на участие в чужих событиях
    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        /*
        В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
         */
        log.info("/users/{userId}/requests PrivateRequestController.getUserRequests(userId {})", userId);
        return requestService.getUserRequests(userId);
        //return "OK from controller of SpringBoot!";
    }


    //Добавление запроса от текущего пользователя на участие в событии
    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED) //201
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                           @RequestParam @Valid Long eventId) {  //(defaultValue = "0")
        /*
        Обратите внимание:

нельзя добавить повторный запрос (Ожидается код ошибки 409)
инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
         */
        log.info("/users/{userId}/requests PrivateRequestController.addParticipationRequest(userId {}, eventId {})", userId, eventId);
        return requestService.addParticipationRequest(userId, eventId);
        //return "OK from controller of SpringBoot!";
    }


    //Отмена своего запроса на участие в событии
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK) //200
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("/users/{userId}/requests/{requestId}/cancel PrivateRequestController.cancelRequest(userId {}, requestId {}):", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
        //return "OK from controller of SpringBoot!";
    }


}

/*ПРИМЕРЫ

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public void create(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("StatsController.create(endpointHitDto {}):", endpointHitDto);

        statsServiceImpl.create(endpointHitDto);

    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("StatsController.getStats(start {}, end {}, uris {}, unique {}):",
                start, end, uris, unique);
        return statsServiceImpl.getStats(start, end, uris, unique);
    }

    @PostMapping
    public BookingResponseDto create(@Validated({Create.class}) @RequestHeader("X-Sharer-User-Id") Long userId,
                                     @RequestBody BookingDto bookingDto) {

        return bookingServiceImpl.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateApproved(@PathVariable Long bookingId,
                             @RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestParam("approved") Boolean isApproved) {
        return bookingServiceImpl.updateApproved(bookingId, userId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findBooking(@PathVariable Long bookingId,
                                  @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingServiceImpl.findBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> findBookingAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(name = "state", defaultValue = "ALL") State state) {
        return bookingServiceImpl.findBookingAll(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> findBookingOwnerAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(name = "state", defaultValue = "ALL") State state) {
        return bookingServiceImpl.findBookingOwnerAll(userId, state);
    }


    //Добавление новой вещи. Будет происходить по эндпоинту POST /items.
    // На вход поступает объект ItemDto. userId в заголовке X-Sharer-User-Id — это идентификатор пользователя,
    // который добавляет вещь. Именно этот пользователь — владелец вещи.
    // Идентификатор владельца будет поступать на вход в каждом из запросов, рассмотренных далее.
    @PostMapping
    public ItemDto create(@Validated({Create.class}) @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemServiceImpl.create(itemDto, userId);
    }

    //Редактирование вещи. Эндпоинт PATCH /items/{itemId}.
    // Изменить можно название, описание и статус доступа к аренде.
    // Редактировать вещь может только её владелец.
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @PathVariable Long itemId,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemServiceImpl.update(itemDto, itemId, userId);
    }

    //Просмотр информации о конкретной вещи по её идентификатору.
    // Эндпоинт GET /items/{itemId}. Информацию о вещи может просмотреть любой пользователь.
    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemServiceImpl.findById(itemId, userId);
    }

    //Просмотр владельцем списка всех его вещей с указанием названия и описания для каждой из них.
    // Эндпоинт GET /items.
    @GetMapping
    public List<ItemDto> findItemsAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemServiceImpl.findItemsAll(userId);
    }

    //Комментарий можно добавить по эндпоинту POST /items/{itemId}/comment,
    //создайте в контроллере метод для него.
    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@Validated({Create.class})
                                    @RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long itemId,
                                    @RequestBody CommentDto commentDto) {

        return itemServiceImpl.createComment(userId, itemId, commentDto);
    }

    //Поиск вещи потенциальным арендатором.
    // Пользователь передаёт в строке запроса текст,
    // и система ищет вещи, содержащие этот текст в названии или описании.
    // Происходит по эндпоинту /items/search?text={text}, в text передаётся текст для поиска.
    // Проверьте, что поиск возвращает только доступные для аренды вещи.
    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestParam String text) {
        return itemServiceImpl.searchByText(text);
    }


    //1.POST /requests — добавить новый запрос вещи.
    //Основная часть запроса — текст запроса, в котором пользователь описывает, какая именно вещь ему нужна.
    @PostMapping
    public ItemRequestResponseDto create(@Validated({Create.class})  @RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestServiceImpl.create(userId, itemRequestDto);
    }

    //2.GET /requests — получить список своих запросов вместе с данными об ответах на них.
    //Для каждого запроса должны быть указаны описание, дата и время создания, а также
    // список ответов в формате: id вещи, название, id владельца.
    // В дальнейшем, используя указанные id вещей, можно будет получить подробную информацию о каждой
    // из них. Запросы должны возвращаться отсортированными от более новых к более старым.
    @GetMapping
    public List<ItemRequestResponseDto> findItemRequestAll(@RequestHeader("X-Sharer-User-Id") Long userId) {

        return itemRequestServiceImpl.findItemRequestAll(userId);
    }


    //3. GET /requests/all — получить список запросов, созданных другими пользователями.
    //С помощью этого эндпоинта пользователи смогут просматривать существующие запросы,
    // на которые они могли бы ответить. Запросы сортируются по дате создания от более новых к
    // более старым.
    @GetMapping("/all")
    public List<ItemRequestResponseDto> findItemRequestOtherAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestServiceImpl.findItemRequestOtherAll(userId);
    }


    //4. GET /requests/{requestId} — получить данные об одном конкретном запросе вместе с данными об
    // ответах на него в том же формате, что и в эндпоинте GET /requests. Посмотреть данные об отдельном
    // запросе может любой пользователь.
    @GetMapping("/{requestId}")
    public ItemRequestResponseDto findItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PathVariable Long requestId) {
        return itemRequestServiceImpl.findItemRequest(userId, requestId);
    }


    @PostMapping
    public UserDto create(@Validated({Create.class}) @RequestBody UserDto userDto) {
        return userServiceImpl.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto userDto,
                          @PathVariable Long userId) {
        return userServiceImpl.update(userDto, userId);
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable Long userId) {
        return userServiceImpl.findById(userId);
    }

    @GetMapping
    public List<UserDto> findUsersAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return userServiceImpl.findUsersAll();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userServiceImpl.delete(userId);
    }
 */