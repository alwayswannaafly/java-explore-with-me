package ru.practicum;

import org.springframework.web.client.RestTemplate;
import ru.practicum.client.StatsClient;
import ru.practicum.client.StatsClientImpl;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public class ClientTest {
    public static void main(String[] args) {
        RestTemplate rt = new RestTemplate();
        StatsClient client = new StatsClientImpl(rt, "http://localhost:9090");

        client.saveHit(new EndpointHit(null, "test-app", "/test", "127.0.0.1", "2025-10-24 12:00:00"));
        List<ViewStats> stats = client.getStats(
                LocalDateTime.of(2025, 10, 24, 11, 0),
                LocalDateTime.of(2025, 10, 24, 13, 0),
                null,
                false
        );
        System.out.println(stats);
    }
}