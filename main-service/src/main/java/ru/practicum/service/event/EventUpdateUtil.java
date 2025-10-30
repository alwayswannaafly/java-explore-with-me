package ru.practicum.service.event;

import jakarta.validation.ValidationException;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.Location;

import java.time.LocalDateTime;

public class EventUpdateUtil {
    public static void updateCommonFields(Event event,
                                          String title,
                                          String annotation,
                                          String description,
                                          Location location,
                                          Boolean paid,
                                          Integer participantLimit,
                                          Boolean requestModeration) {

        if (title != null) {
            if (title.length() < 3 || title.length() > 120) {
                throw new ValidationException("Title must be between 3 and 120 characters");
            }
            event.setTitle(title);
        }

        if (annotation != null) {
            if (annotation.length() < 20 || annotation.length() > 2000) {
                throw new ValidationException("Annotation must be between 20 and 2000 characters");
            }
            event.setAnnotation(annotation);
        }

        if (description != null) {
            if (description.length() < 20 || description.length() > 7000) {
                throw new ValidationException("Description must be between 20 and 7000 characters");
            }
            event.setDescription(description);
        }

        if (location != null) event.setLocation(location);
        if (paid != null) event.setPaid(paid);
        if (participantLimit != null) {
            if (participantLimit < 0) {
                throw new ValidationException("participantLimit can't be less than 0");
            }
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) event.setRequestModeration(requestModeration);
    }

    public static void validateEventDate(LocalDateTime eventDate, int minHours) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(minHours))) {
            throw new ValidationException("Event date must be at least " + minHours + " hour after publication");
        }
    }
}
