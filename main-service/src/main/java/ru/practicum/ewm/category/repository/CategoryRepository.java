package ru.practicum.ewm.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.category.model.Category;

import java.util.Optional;
//import ru.practicum.ewm.category.model.Test;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    //...
    //запросный метод + ассоциация Booker.Id + сортировочный объект
    Optional<Category> findFirst1ById(Long id);

    Boolean existsByIdNotAndNameIs(Long catId, String name);

}

/*ПРИМЕРЫ

    //запросный метод + ассоциация Booker.Id + сортировочный объект
    List<Booking> findAllByBookerId(Long userId, Sort sort);

    //List<Booking> findByBooker_IdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);
    //запросный метод + ассоциация Booker.Id OrderByStartDesc
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime datetimeNowS, LocalDateTime datetimeNowE); //, Sort sort);  //запросный метод

    //LessThanEqual запросный метод без ассоциации Booker.Id
    List<Booking> findAllByBookerAndStartAfterAndEndAfterOrderByStartDesc(User user, LocalDateTime datetimeNowS, LocalDateTime datetimeNowE);

    //jpql + ассоциация Booker.id join User.id + параметр ?1 + параметр :datetimeNow + сортировка
    @Query(" select b from Booking b inner join User u on b.booker.id = u.id " +
            " where u.id = :userId " +
            " and b.start > :datetimeNow " +
            " order by b.start DESC")
    List<Booking> findJpqlQueryFuture(Long userId, LocalDateTime datetimeNow);

    //where b.booker.id = ?1   and b.start > :datetimeNow and b.state = ?3
    //jpql + ассоциация Booker.Id + параметр ?1 + параметр :datetimeNow + сортировка
    @Query(" select b from Booking b " +
            " where b.booker.id = :userId " +
            " and b.start > :datetimeNow and b.status = :status" +
            " order by b.start DESC")
    List<Booking> findJpqlQueryStatus(Long userId, LocalDateTime datetimeNow, Status status); //StatusEquals

    //запросный метод + ассоциация Item.Owner.Id + сортировочный объект
    List<Booking> findAllByItemOwnerId(Long userId, Sort sort);

    //запросный метод + ассоциация Item.Owner.Id
    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime datetimeNowS, LocalDateTime datetimeNowE); //, Sort sort);  //запросный метод

    //запросный метод без ассоциации Item.Owner.Id
    List<Booking> findAllByItemOwnerAndStartAfterAndEndAfterOrderByStartDesc(User user, LocalDateTime datetimeNowS, LocalDateTime datetimeNowE);

    //jpql + ассоциация Item.Owner.Id join User.id + параметр ?1 + параметр :datetimeNow + сортировка
    @Query(" select b from Booking b " +
            " inner join Item i on b.item.id = i.id " +
            " where i.owner.id = :userId " +
            " and b.start > :datetimeNow " +
            " order by b.start DESC")
    List<Booking> findJpqlQueryFutureOwner(Long userId, LocalDateTime datetimeNow);

    //and b.start > :datetimeNow and b.state = ?3
    //jpql + ассоциация Item.Owner.Id + параметр ?1 + параметр :datetimeNow + сортировка
    @Query(" select b from Booking b " +
            " inner join Item i on b.item.id = i.id " +
            " where i.owner.id = :userId " +
            " and b.start > :datetimeNow and b.status = :status" +
            " order by b.start DESC")
    List<Booking> findJpqlQueryStatusOwner(Long userId, LocalDateTime datetimeNow, Status status);

    Booking findFirst1ByItemAndStartBeforeOrderByStartDesc(Item item, LocalDateTime datetimeNow);

    Booking findFirst1ByItemAndStartAfterOrderByStartAsc(Item item, LocalDateTime datetimeNow);

    List<Booking> findAllByBookerAndEndIsBeforeAndItemAndStatusEquals(User user, LocalDateTime datetimeNow,
                                                                    Item item, Status status);


List<Comment> findAllByItem(Item item);

    List<Item> findAllByOwnerId(Long userId);

    @Query(" select i from Item i " +
            " where (upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            " and i.available = true ")
    List<Item> searchByText(String text);

    //--List<Item> findAllByRequestId(Long requestId);
    List<Item> findAllByRequest(Long requestId);


    List<ItemRequest> findAllByRequestorOrderByCreatedDesc(User requestor);

    List<ItemRequest> findAllByRequestorNotOrderByCreatedDesc(User requestor);

    ItemRequest findAllById(Long id);



    @Query("SELECT new ru.practicum.dto.ViewStatsDto(s.app, s.uri, COUNT(DISTINCT s.ip)) " +
            "FROM Stat AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<ViewStatsDto> getUrisAllUniqTrue(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.ViewStatsDto(s.app, s.uri, COUNT(s.ip)) " +
            "FROM Stat AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.ip) DESC")
    List<ViewStatsDto> getUrisAllUniqFalse(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.ViewStatsDto(s.app, s.uri, COUNT(DISTINCT s.ip)) " +
            "FROM Stat AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "AND s.uri IN (:uris) " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<ViewStatsDto> getUrisByUniqTrue(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.dto.ViewStatsDto(s.app, s.uri, COUNT(s.ip)) " +
            "FROM Stat AS s " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "AND s.uri IN (:uris) " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.ip) DESC")
    List<ViewStatsDto> getUrisByUniqFalse(LocalDateTime start, LocalDateTime end, List<String> uris);

 */