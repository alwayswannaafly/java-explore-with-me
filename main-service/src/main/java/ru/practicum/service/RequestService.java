package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventState;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        // опубликовано?
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event with id=" + eventId + " is not published");
        }

        // запрос себе
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request own event");
        }

        // unique
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exists");
        }

        // limit
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }


        Request request = Request.builder()
                .requester(requester)
                .event(event)
                .status(RequestStatus.PENDING)
                .build();

        // авто
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        Request saved = requestRepository.save(request);
        return RequestMapper.toDto(saved);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (!request.getRequester().getId().equals(userId)) {
            throw new ForbiddenException("Not your request");
        }
        if (request.getStatus() == RequestStatus.CONFIRMED || request.getStatus() == RequestStatus.PENDING) {
            request.setStatus(RequestStatus.CANCELED);
            return RequestMapper.toDto(requestRepository.save(request));
        }
        throw new ConflictException("Cannot cancel request in status: " + request.getStatus());
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Not your event");
        }
        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult updateRequests(
            Long userId, Long eventId, EventRequestStatusUpdateRequest update) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Not your event");
        }

        List<Request> requests = requestRepository.findAllById(update.getRequestIds());
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = event.getParticipantLimit();

        for (Request req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Only PENDING requests can be updated");
            }

            if ("CONFIRMED".equals(update.getStatus())) {
                if (limit > 0 && confirmedCount >= limit) {
                    throw new ConflictException("Participant limit reached");
                }
                req.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
                confirmed.add(RequestMapper.toDto(req));
            } else {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(req));
            }
        }

        if (limit > 0 && confirmedCount >= limit) {
            List<Request> pending = requestRepository.findByEventId(eventId).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .collect(Collectors.toList());
            for (Request p : pending) {
                p.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(p));
            }
            requestRepository.saveAll(pending);
        }

        requestRepository.saveAll(requests);
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }
}
