package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategoryId(Long categoryId);

    Set<Event> findAllByIdIn(Set<Long> eventIdList);

    // для EventService
    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<Event> findByIdAndState(Long eventId, EventState eventStatus);

    @Query(value = "SELECT * FROM events e " +
            "WHERE (:text is null OR lower(e.annotation) LIKE lower(concat('%',cast(:text AS text),'%')) " +
            "OR lower(e.description) LIKE lower(concat('%',cast(:text AS text),'%')))" +
            "AND (:categories is null or e.category_id IN (:categories)) " +
            "AND e.state = 'PUBLISHED' " +
            "AND (:paid is null or e.paid = :paid) " +
            "AND (e.event_date >= :rangeStart) " +
            "AND (:rangeEnd is null or e.event_date < :rangeEnd) ", nativeQuery = true)
    Page<Event> searchPublishedEvents(@Param("text") String text,
                                      @Param("categories") List<Long> categories,
                                      @Param("paid") Boolean paid,
                                      @Param("rangeStart") LocalDateTime start,
                                      @Param("rangeEnd") LocalDateTime end,
                                      Pageable pageable);

    @Query(value = "SELECT * FROM events e " +
            "WHERE (:userId is null or e.initiator_id IN :userIds) " +
            "AND (:states is null or e.state IN :states) " +
            "AND (:categories is null or e.category_id IN :categories) " +
            "AND (:rangeStart is null or e.event_date >= :rangeStart) " +
            "AND (:rangeEnd is null or e.event_date < :rangeEnd) ", nativeQuery = true)
    Page<Event> findEvents(@Param("userIds") List<Long> userIds,
                           @Param("states") List<EventState> states,
                           @Param("categories") List<Long> categories,
                           @Param("rangeStart") LocalDateTime rangeStart,
                           @Param("rangeEnd") LocalDateTime rangeEnd,
                           Pageable pageable);
}