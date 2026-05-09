package ru.practicum.ewm.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {
	public Event toEntity(NewEventDto dto, User initiator, Category category) {
		return Event.builder()
				.annotation(dto.getAnnotation())
				.description(dto.getDescription())
				.title(dto.getTitle())
				.eventDate(dto.getEventDate())
				.createdOn(LocalDateTime.now())
				.paid(Boolean.TRUE.equals(dto.getPaid()))
				.participantLimit(dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit())
				.requestModeration(dto.getRequestModeration() == null || dto.getRequestModeration())
				.state(EventState.PENDING)
				.location(toLocation(dto.getLocation()))
				.category(category)
				.initiator(initiator)
				.build();
	}

	public EventShortDto toShortDto(Event event, Long confirmedRequests, Long views) {
		return EventShortDto.builder()
				.annotation(event.getAnnotation())
				.category(CategoryMapper.toDto(event.getCategory()))
				.confirmedRequests(confirmedRequests)
				.eventDate(event.getEventDate())
				.id(event.getId())
				.initiator(UserMapper.toShortDto(event.getInitiator()))
				.paid(event.getPaid())
				.title(event.getTitle())
				.views(views)
				.build();
	}

	public EventFullDto toFullDto(Event event, Long confirmedRequests, Long views) {
		return EventFullDto.builder()
				.annotation(event.getAnnotation())
				.category(CategoryMapper.toDto(event.getCategory()))
				.confirmedRequests(confirmedRequests)
				.createdOn(event.getCreatedOn())
				.description(event.getDescription())
				.eventDate(event.getEventDate())
				.id(event.getId())
				.initiator(UserMapper.toShortDto(event.getInitiator()))
				.location(toLocationDto(event.getLocation()))
				.paid(event.getPaid())
				.participantLimit(event.getParticipantLimit())
				.publishedOn(event.getPublishedOn())
				.requestModeration(event.getRequestModeration())
				.state(event.getState())
				.title(event.getTitle())
				.views(views)
				.build();
	}

	public Location toLocation(LocationDto dto) {
		return Location.builder()
				.lat(dto.getLat())
				.lon(dto.getLon())
				.build();
	}

	public LocationDto toLocationDto(Location location) {
		return LocationDto.builder()
				.lat(location.getLat())
				.lon(location.getLon())
				.build();
	}
}