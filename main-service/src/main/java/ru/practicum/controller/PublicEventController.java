package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.event.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final PublicEventService publicEventService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(defaultValue = "") List<Long> categories,
            @RequestParam(defaultValue = "") Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "") String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        publicEventService.saveHit(request);

        return publicEventService.getPublicEvents(
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size
        );
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(
            @PathVariable Long id,
            HttpServletRequest request) {

        publicEventService.saveHit(request);

        return publicEventService.getPublicEventById(id);
    }
}
