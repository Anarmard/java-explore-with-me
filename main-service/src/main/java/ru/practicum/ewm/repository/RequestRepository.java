package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.enums.RequestStatus;
import ru.practicum.ewm.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByIdAndRequesterId(Long userId, Long requestId);

    Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByIdIn(List<Long> requestIdList);

    Long countByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    @Query("select r from Request r " +
            "join fetch r.event e " +
            "where r.requester.id = ?1 " +
            "and e.initiator.id <> ?1")
    List<Request> findAllByRequesterIdAndNotInitiator(Long userId);

    @Query("select r from Request r " +
            "join fetch r.event e " +
            "where e.initiator.id = ?1 " +
            "and e.id <> ?2")
    List<Request> findAllRequestsForEventInitiator(Long userId, Long eventId);
}
