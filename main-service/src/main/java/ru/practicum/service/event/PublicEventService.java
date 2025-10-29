package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.specification.EventSpecifications;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicEventService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private final StatsService statsService;

    public void saveHit(HttpServletRequest request) {
        EndpointHit hit = new EndpointHit();
        hit.setApp("ewm-main-service");
        hit.setUri(request.getRequestURI());
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        statsClient.saveHit(hit);
    }

    public List<EventShortDto> getPublicEvents(
            String text,
            List<Long> categoryIds,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            Integer from,
            Integer size) {

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("rangeEnd must be after rangeStart");
        }

        Sort sortConfig = "VIEWS".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "id")
                : Sort.by(Sort.Direction.ASC, "eventDate");

        Pageable pageable = PageRequest.of(from / size, size, sortConfig);

        Specification<Event> spec = EventSpecifications.isPublished()
                .and(EventSpecifications.hasText(text))
                .and(EventSpecifications.inCategories(categoryIds))
                .and(EventSpecifications.isPaid(paid))
                .and(EventSpecifications.eventDateAfter(rangeStart))
                .and(EventSpecifications.eventDateBefore(rangeEnd));

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        if (onlyAvailable != null && onlyAvailable) {
            Map<Long, Long> confirmedMap = statsService.getConfirmedRequestsCount(events);
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() == 0 ||
                            e.getParticipantLimit() > confirmedMap.getOrDefault(e.getId(), 0L))
                    .collect(Collectors.toList());
        }

        Map<Long, Long> viewsMap = statsService.getViewsCount(events);
        Map<Long, Long> confirmedMap = statsService.getConfirmedRequestsCount(events);

        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        viewsMap.getOrDefault(e.getId(), 0L),
                        confirmedMap.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    public EventFullDto getPublicEventById(Long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        long views = statsService.getViewsCount(List.of(event)).getOrDefault(id, 0L);
        long confirmed = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);

        return EventMapper.toEventFullDto(event, views, confirmed);
    }
}
