package ru.practicum.service.event;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.request.UpdateEventAdminRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.specification.AdminEventSpecifications;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventService {
    private final EventRepository eventRepository;
    private final StatsService statsService;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        EventUpdateUtil.updateCommonFields(
                event,
                request.getTitle(),
                request.getAnnotation(),
                request.getDescription(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration()
        );

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }

        EventUpdateUtil.validateEventDate(request.getEventDate(), 1);

        handleAdminStateAction(event, request.getStateAction());

        Event updated = eventRepository.save(event);
        long views = statsService.getViewsCount(List.of(updated)).getOrDefault(eventId, 0L);
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        return EventMapper.toEventFullDto(updated, views, confirmed);
    }

    private void handleAdminStateAction(Event event, String stateAction) {
        if ("PUBLISH_EVENT".equals(stateAction)) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish event not in PENDING state");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if ("REJECT_EVENT".equals(stateAction)) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot reject published event");
            }
            event.setState(EventState.CANCELED);
        }
    }

    public List<EventFullDto> searchEvents(List<Long> userIds, List<String> states, List<Long> categoryIds, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        if (rangeEnd != null && rangeStart == null) {
            throw new ValidationException("rangeStart must be specified if rangeEnd is set");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<EventState> eventStates = null;
        if (states != null && !states.isEmpty()) {
            eventStates = states.stream()
                    .map(String::toUpperCase)
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        Specification<Event> spec = Specification
                .where(AdminEventSpecifications.byUserIds(userIds))
                .and(AdminEventSpecifications.byStates(eventStates))
                .and(AdminEventSpecifications.byCategoryIds(categoryIds))
                .and(AdminEventSpecifications.eventDateAfter(rangeStart))
                .and(AdminEventSpecifications.eventDateBefore(rangeEnd));

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        Map<Long, Long> viewsMap = statsService.getViewsCount(events);
        Map<Long, Long> confirmedMap = statsService.getConfirmedRequestsCount(events);

        return events.stream()
                .map(e -> EventMapper.toEventFullDto(
                        e,
                        viewsMap.getOrDefault(e.getId(), 0L),
                        confirmedMap.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }
}
