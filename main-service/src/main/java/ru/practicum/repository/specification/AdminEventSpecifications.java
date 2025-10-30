package ru.practicum.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;

import java.time.LocalDateTime;
import java.util.List;

public class AdminEventSpecifications {

    public static Specification<Event> byUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return null;
        return (root, query, cb) -> root.get("initiator").get("id").in(userIds);
    }

    public static Specification<Event> byStates(List<EventState> states) {
        if (states == null || states.isEmpty()) return null;
        return (root, query, cb) -> root.get("state").in(states);
    }

    public static Specification<Event> byCategoryIds(List<Long> categoryIds) {
        return EventSpecifications.inCategories(categoryIds);
    }

    public static Specification<Event> eventDateAfter(LocalDateTime rangeStart) {
        return EventSpecifications.eventDateAfter(rangeStart);
    }

    public static Specification<Event> eventDateBefore(LocalDateTime rangeEnd) {
        return EventSpecifications.eventDateBefore(rangeEnd);
    }
}
