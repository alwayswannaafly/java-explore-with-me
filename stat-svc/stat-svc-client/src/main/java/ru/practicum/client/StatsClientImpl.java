package ru.practicum.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StatsClientImpl implements StatsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClientImpl(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public void saveHit(EndpointHit hit) {
        String url = baseUrl + "/hit";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHit> request = new HttpEntity<>(hit, headers);
        restTemplate.postForEntity(url, request, Void.class);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String startStr = start.format(FORMATTER);
        String endStr = end.format(FORMATTER);

        String encodedStart = URLEncoder.encode(startStr, StandardCharsets.UTF_8);
        String encodedEnd = URLEncoder.encode(endStr, StandardCharsets.UTF_8);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/stats")
                .queryParam("start", encodedStart)
                .queryParam("end", encodedEnd);

        if (uris != null && !uris.isEmpty()) {
            String encodedUris = uris.stream()
                    .map(uri -> URLEncoder.encode(uri, StandardCharsets.UTF_8))
                    .collect(Collectors.joining(","));
            builder.queryParam("uris", encodedUris);
        }

        if (unique != null) {
            builder.queryParam("unique", unique);
        }

        URI uri = builder.build(true).toUri();
        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(uri, ViewStats[].class);
        return Arrays.asList(response.getBody());
    }
}
