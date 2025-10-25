package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.controller.StatsController;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void hit_ShouldReturnCreated() throws Exception {
        // Given
        EndpointHit hit = new EndpointHit(null, "app", "/uri",
                "192.168.0.1", "2023-10-20 10:00:00");

        doNothing().when(statsService).saveHit(any(EndpointHit.class));

        // When & Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hit)))
                .andExpect(status().isCreated()); // HttpStatus.CREATED = 201


        verify(statsService).saveHit(any(EndpointHit.class));
    }

    @Test
    void getStats_ShouldReturnStats() throws Exception {
        // Given
        LocalDateTime start = LocalDateTime.of(2023, 10, 20, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 21, 10, 0, 0);
        List<String> uris = Arrays.asList("/test1", "/test2");
        Boolean unique = false;

        List<ViewStats> mockStats = Arrays.asList(
                new ViewStats("app1", "/test1", 5L),
                new ViewStats("app2", "/test2", 3L)
        );

        when(statsService.getStats(eq(start), eq(end), eq(uris), eq(unique))).thenReturn(mockStats);

        String params = String.format(
                "/stats?start=%s&end=%s&uris=%s&unique=%s",
                "2023-10-20 10:00:00",
                "2023-10-21 10:00:00",
                String.join(",", uris),
                unique
        );

        // When & Then
        mockMvc.perform(get(params)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("app1"))
                .andExpect(jsonPath("$[0].uri").value("/test1"))
                .andExpect(jsonPath("$[0].hits").value(5L))
                .andExpect(jsonPath("$[1].app").value("app2"))
                .andExpect(jsonPath("$[1].uri").value("/test2"))
                .andExpect(jsonPath("$[1].hits").value(3L));


        verify(statsService).getStats(eq(start), eq(end), eq(uris), eq(unique));
    }

    @Test
    void getStats_WithDefaultUnique_ShouldCallServiceWithFalse() throws Exception {
        // Given
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), eq(false)))
                .thenReturn(List.of());

        String params = String.format(
                "/stats?start=%s&end=%s",
                "2023-10-20 10:00:00",
                "2023-10-21 10:00:00"
        );

        // When & Then
        mockMvc.perform(get(params))
                .andExpect(status().isOk());

        verify(statsService).getStats(any(LocalDateTime.class), any(LocalDateTime.class), any(), eq(false));
    }
}