package ru.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.DateValidationException;
import ru.practicum.model.HitEntity;
import ru.practicum.repository.HitRepository;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private HitRepository hitRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(hitRepository);
    }

    @Test
    void saveHit_ShouldCallRepositorySave_WithCorrectEntity() {
        // Given
        String app = "app";
        String uri = "uri";
        String ip = "192.168.0.1";
        String timestamp = "2023-10-20 10:00:00";
        EndpointHit dto = new EndpointHit(null, app, uri, ip, timestamp);

        ArgumentCaptor<HitEntity> captor = ArgumentCaptor.forClass(HitEntity.class);

        // When
        statsService.saveHit(dto);

        // Then
        verify(hitRepository).save(captor.capture());
        HitEntity savedEntity = captor.getValue();
        assertEquals(app, savedEntity.getApp());
        assertEquals(uri, savedEntity.getUri());
        assertEquals(ip, savedEntity.getIp());
        assertEquals(LocalDateTime.of(2023, 10, 20, 10, 0, 0), savedEntity.getTimestamp());
    }

    @Test
    void getStats_WhenStartAfterEnd_ShouldThrowDateValidationException() {
        // Given
        LocalDateTime start = LocalDateTime.of(2023, 10, 21, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 20, 10, 0);
        List<String> uris = Collections.singletonList("/test");
        Boolean unique = false;

        // When & Then
        assertThrows(DateValidationException.class, () -> statsService.getStats(start, end, uris, unique));
    }

    @Test
    void getStats_WhenValidInputAndNotUnique_ShouldReturnStats() {
        // Given
        LocalDateTime start = LocalDateTime.of(2023, 10, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 21, 10, 0);
        List<String> uris = Arrays.asList("/test1", "/test2");
        Boolean unique = false;

        List<Object[]> dbResults = Arrays.asList(
                new Object[]{"app1", "/test1", 5L},
                new Object[]{"app2", "/test2", 3L}
        );
        when(hitRepository.findStats(start, end, uris)).thenReturn(dbResults);

        // When
        List<ViewStats> result = statsService.getStats(start, end, uris, unique);

        // Then
        assertEquals(2, result.size());
        assertEquals("app1", result.getFirst().getApp());
        assertEquals("/test1", result.get(0).getUri());
        assertEquals(5L, result.get(0).getHits());
        assertEquals("app2", result.get(1).getApp());
        assertEquals("/test2", result.get(1).getUri());
        assertEquals(3L, result.get(1).getHits());

        verify(hitRepository).findStats(eq(start), eq(end), eq(uris));
        verify(hitRepository, never()).findUniqueStats(any(), any(), any());
    }

    @Test
    void getStats_WhenValidInputAndUnique_ShouldReturnUniqueStats() {
        // Given
        LocalDateTime start = LocalDateTime.of(2023, 10, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 21, 10, 0);
        List<String> uris = List.of("/test1");
        Boolean unique = true;

        List<Object[]> dbResults = Arrays.asList(
                new Object[]{"app1", "/test1", 2L},
                new Object[]{"app2", "/test2", 3L}
        );
        when(hitRepository.findUniqueStats(start, end, uris)).thenReturn(dbResults);

        // When
        List<ViewStats> result = statsService.getStats(start, end, uris, unique);

        // Then
        assertEquals(2, result.size());
        assertEquals("app1", result.getFirst().getApp());
        assertEquals("/test1", result.getFirst().getUri());
        assertEquals(2L, result.getFirst().getHits());

        verify(hitRepository).findUniqueStats(eq(start), eq(end), eq(uris));
        verify(hitRepository, never()).findStats(any(), any(), any());
    }

    @Test
    void getStats_WhenUrisIsNull_ShouldPassNullToRepository() {
        // Given
        LocalDateTime start = LocalDateTime.of(2023, 10, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 21, 10, 0);
        Boolean unique = false;

        List<Object[]> dbResults = Collections.singletonList(new Object[]{"app1", "/", 10L});
        when(hitRepository.findStats(start, end, null)).thenReturn(dbResults);

        // When
        List<ViewStats> result = statsService.getStats(start, end, null, unique);

        // Then
        assertEquals(1, result.size());
        verify(hitRepository).findStats(eq(start), eq(end), isNull());
    }

    @Test
    void getStats_WhenUrisIsEmpty_ShouldPassEmptyListToRepository() {
        // Given
        LocalDateTime start = LocalDateTime.of(2023, 10, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 10, 21, 10, 0);
        List<String> uris = Collections.emptyList();
        Boolean unique = false;

        List<Object[]> dbResults = Collections.emptyList();
        when(hitRepository.findStats(start, end, uris)).thenReturn(dbResults);

        // When
        List<ViewStats> result = statsService.getStats(start, end, uris, unique);

        // Then
        assertTrue(result.isEmpty());
        verify(hitRepository).findStats(eq(start), eq(end), eq(uris));
    }
}