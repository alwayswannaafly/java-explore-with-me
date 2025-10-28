package ru.practicum.mapper;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompilationMapper {
    public static Compilation toCompilation(NewCompilationDto dto, List<Event> events) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(events)
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation c) {
        CompilationDto dto = new CompilationDto();
        dto.setTitle(c.getTitle());
        dto.setPinned(c.getPinned());
        dto.setId(c.getId());
        dto.setEvents(c.getEvents().stream()
                .map(event -> EventMapper.toEventShortDto(
                        event, 0L, 0L
                ))
                .collect(Collectors.toList()));
        return dto;
    }

    public static CompilationDto toCompilationDtoWithViews(
            Compilation compilation,
            Map<Long, Long> viewsMap,
            Map<Long, Long> confirmedRequestsMap) {

        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.getPinned());

        List<Event> events = compilation.getEvents();
        if (events.isEmpty()) {
            dto.setEvents(List.of());
        } else {
            dto.setEvents(events.stream()
                    .map(e -> EventMapper.toEventShortDto(
                            e,
                            viewsMap.getOrDefault(e.getId(), 0L),
                            confirmedRequestsMap.getOrDefault(e.getId(), 0L)
                    ))
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
