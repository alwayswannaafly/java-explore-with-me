package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.HitEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<HitEntity, Long> {
    @Query("SELECT h.app, h.uri, COUNT(h) FROM HitEntity h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h) DESC")
    List<Object[]> findStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("uris") List<String> uris);

    @Query("SELECT h.app, h.uri, COUNT(DISTINCT h.ip) FROM HitEntity h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<Object[]> findUniqueStats(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("uris") List<String> uris);
}
