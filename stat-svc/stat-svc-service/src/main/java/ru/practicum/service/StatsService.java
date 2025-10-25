package ru.practicum.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.DateValidationException;
import ru.practicum.model.HitEntity;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final HitRepository hitRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsService(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    @Transactional
    public void saveHit(EndpointHit dto) {
        HitEntity entity = new HitEntity();
        entity.setApp(dto.getApp());
        entity.setUri(dto.getUri());
        entity.setIp(dto.getIp());
        entity.setTimestamp(LocalDateTime.parse(dto.getTimestamp(), FORMATTER));
        hitRepository.save(entity);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new DateValidationException("Start time must be before or equal to end time");
        }

        List<Object[]> results;
        if (Boolean.TRUE.equals(unique)) {
            results = hitRepository.findUniqueStats(start, end, uris);
        } else {
            results = hitRepository.findStats(start, end, uris);
        }

        return results.stream()
                .map(row -> new ViewStats(
                        (String) row[0], // app
                        (String) row[1], // uri
                        ((Number) row[2]).longValue() // hits
                ))
                .collect(Collectors.toList());
    }
}