package ru.practicum.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.model.event.Event;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecifications {

    public static Specification<Event> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("state"), "PUBLISHED");
    }

    public static Specification<Event> hasText(String text) {
        if (text == null || text.isBlank()) return null;
        String pattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("annotation")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<Event> inCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return null;
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<Event> isPaid(Boolean paid) {
        if (paid == null) return null;
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> eventDateAfter(LocalDateTime rangeStart) {
        if (rangeStart == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart);
    }

    public static Specification<Event> eventDateBefore(LocalDateTime rangeEnd) {
        if (rangeEnd == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd);
    }
}