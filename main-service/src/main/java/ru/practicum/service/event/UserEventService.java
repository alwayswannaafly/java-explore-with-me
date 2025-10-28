package ru.practicum.service.event;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.request.UpdateEventUserRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserEventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsService statsService;

    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date must be at least 2 hours in the future");
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + dto.getCategory() + " not found"));

        Event event = EventMapper.toEvent(dto, initiator, category);
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(savedEvent, 0L, 0L);
    }

    public List<EventFullDto> getUserEvents(Long userId, Pageable pageable) {
        Page<Event> page = eventRepository.findByInitiator_Id(userId, pageable);
        List<Event> events = page.getContent();

        Map<Long, Long> confirmedMap = statsService.getConfirmedRequestsCount(events);
        Map<Long, Long> viewsMap = statsService.getViewsCount(events);

        return events.stream()
                .map(e -> EventMapper.toEventFullDto(e, viewsMap.getOrDefault(e.getId(), 0L),
                        confirmedMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        long views = statsService.getViewsCount(List.of(event)).getOrDefault(eventId, 0L);
        long confirmed = statsService.getConfirmedRequestsCount(List.of(event)).getOrDefault(eventId, 0L);
        return EventMapper.toEventFullDto(event, views, confirmed);
    }

    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

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

        EventUpdateUtil.validateEventDate(request.getEventDate(), 2);

        handleUserStateAction(event, request.getStateAction());

        Event updated = eventRepository.save(event);
        long views = statsService.getViewsCount(List.of(updated)).getOrDefault(eventId, 0L);
        long confirmed = statsService.getConfirmedRequestsCount(List.of(updated)).getOrDefault(eventId, 0L);

        return EventMapper.toEventFullDto(updated, views, confirmed);
    }

    private void handleUserStateAction(Event event, String stateAction) {
        if ("SEND_TO_REVIEW".equals(stateAction)) {
            event.setState(EventState.PENDING);
        } else if ("CANCEL_REVIEW".equals(stateAction)) {
            event.setState(EventState.CANCELED);
        }
    }
}
