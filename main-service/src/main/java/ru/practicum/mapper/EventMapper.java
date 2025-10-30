package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.Category;
import ru.practicum.model.User;
import ru.practicum.model.event.Event;

@UtilityClass
public class EventMapper {

    public static Event toEvent(NewEventDto dto, User initiator, Category category) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setCategory(category);
        event.setAnnotation(dto.getAnnotation());
        event.setEventDate(dto.getEventDate());
        event.setLocation(dto.getLocation());
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setInitiator(initiator);
        return event;
    }

    public static EventFullDto toEventFullDto(Event event, long views, long confirmedRequests) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setLocation(event.getLocation());
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState().name());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setViews(views);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        return dto;
    }

    public static EventShortDto toEventShortDto(Event event, long views, long confirmedRequests) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.getPaid());
        dto.setViews(views);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        return dto;
    }
}
