package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.request.RequestCountDto;
import ru.practicum.model.event.Event;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsClient statsClient;
    private final RequestRepository requestRepository;

    public Map<Long, Long> getConfirmedRequestsCount(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();

        List<RequestCountDto> results = requestRepository.countConfirmedByEventIds(ids);

        return results.stream()
                .collect(Collectors.toMap(RequestCountDto::eventId, RequestCountDto::count));
    }

    public Map<Long, Long> getViewsCount(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        List<ViewStats> stats = statsClient.getStats(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                now,
                uris,
                true
        );

        Map<String, Long> uriToHits = stats.stream()
                .collect(Collectors.toMap(
                        ViewStats::getUri,
                        ViewStats::getHits,
                        (a, b) -> a
                ));

        Map<Long, Long> eventIdToViews = new HashMap<>();
        for (Event event : events) {
            String uri = "/events/" + event.getId();
            eventIdToViews.put(event.getId(), uriToHits.getOrDefault(uri, 0L));
        }

        return eventIdToViews;
    }
}
