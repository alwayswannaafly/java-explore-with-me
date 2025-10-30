package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.request.RequestCountDto;
import ru.practicum.model.Request;
import ru.practicum.model.request.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    long countByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    @Query("""
            SELECT new ru.practicum.dto.request.RequestCountDto(r.event.id, COUNT(r))
            FROM Request r
            WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED'
            GROUP BY r.event.id""")
    List<RequestCountDto> countConfirmedByEventIds(@Param("eventIds") List<Long> eventIds);

    List<Request> findByRequesterId(Long userId);

    List<Request> findByEventId(Long eventId);
}
