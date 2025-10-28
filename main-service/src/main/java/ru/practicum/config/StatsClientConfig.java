package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.client.StatsClient;
import ru.practicum.client.StatsClientImpl;

@Configuration
public class StatsClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StatsClient statsClient(RestTemplate restTemplate, @Value("${stats.client.base-url}") String baseUrl) {
        return new StatsClientImpl(restTemplate, baseUrl);
    }
}