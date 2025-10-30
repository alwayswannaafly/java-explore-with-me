package ru.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.event.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatsService statsService;

    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        List<Event> events = dto.getEvents() == null ? List.of() :
                eventRepository.findAllById(dto.getEvents());
        Compilation compilation = CompilationMapper.toCompilation(dto, events);
        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(saved);
    }

    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        if (request.getTitle() != null) {
            if (request.getTitle().isEmpty() || request.getTitle().length() > 50) {
                throw new ValidationException("Title length should be between 1 and 50 characters");
            }
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) compilation.setPinned(request.getPinned());
        if (request.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(request.getEvents());
            compilation.setEvents(events);
        }

        Compilation updated = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(updated);
    }


    public CompilationDto getPublicCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        Map<Long, Long> viewsMap = statsService.getViewsCount(compilation.getEvents());
        Map<Long, Long> confirmedMap = statsService.getConfirmedRequestsCount(compilation.getEvents());

        return CompilationMapper.toCompilationDtoWithViews(compilation, viewsMap, confirmedMap);
    }

    public List<CompilationDto> getPublicCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(pageable).getContent()
                : compilationRepository.findByPinned(pinned, pageable);

        List<Event> allEvents = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Long> viewsMap = statsService.getViewsCount(allEvents);
        Map<Long, Long> confirmedMap = statsService.getConfirmedRequestsCount(allEvents);

        return compilations.stream()
                .map(c -> CompilationMapper.toCompilationDtoWithViews(c, viewsMap, confirmedMap))
                .collect(Collectors.toList());
    }
}