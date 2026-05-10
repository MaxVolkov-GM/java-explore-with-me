package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.UserStateAction;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPrivateService {
	private static final Long DEFAULT_CONFIRMED_REQUESTS = 0L;
	private static final Long DEFAULT_VIEWS = 0L;

	private final EventRepository eventRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final EventStatsService eventStatsService;
	private final EventValidationService eventValidationService;

	@Transactional
	public EventFullDto create(Long userId, NewEventDto dto) {
		eventValidationService.validateEventDateForUser(dto.getEventDate());

		User initiator = getUserOrThrow(userId);
		Category category = getCategoryOrThrow(dto.getCategory());

		Event event = EventMapper.toEntity(dto, initiator, category);
		Event savedEvent = eventRepository.save(event);

		return EventMapper.toFullDto(savedEvent, DEFAULT_CONFIRMED_REQUESTS, DEFAULT_VIEWS);
	}

	public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
		getUserOrThrow(userId);

		Pageable pageable = new OffsetPageRequest(from, size);
		List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
		Map<Long, Long> confirmedRequests = eventStatsService.getConfirmedRequests(events);

		return events.stream()
				.map(event -> EventMapper.toShortDto(
						event,
						confirmedRequests.getOrDefault(event.getId(), DEFAULT_CONFIRMED_REQUESTS),
						DEFAULT_VIEWS
				))
				.collect(Collectors.toList());
	}

	public EventFullDto getUserEvent(Long userId, Long eventId) {
		getUserOrThrow(userId);

		Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Event with id=%d was not found", eventId)
				));

		Long confirmedRequests = eventStatsService.getConfirmedRequests(eventId);

		return EventMapper.toFullDto(event, confirmedRequests, DEFAULT_VIEWS);
	}

	@Transactional
	public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
		getUserOrThrow(userId);

		Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Event with id=%d was not found", eventId)
				));

		if (event.getState() == EventState.PUBLISHED) {
			throw new ConflictException("Only pending or canceled events can be changed");
		}

		applyUserUpdate(event, request);

		Event updatedEvent = eventRepository.save(event);
		Long confirmedRequests = eventStatsService.getConfirmedRequests(eventId);

		return EventMapper.toFullDto(updatedEvent, confirmedRequests, DEFAULT_VIEWS);
	}

	private void applyUserUpdate(Event event, UpdateEventUserRequest request) {
		if (request.getAnnotation() != null) {
			event.setAnnotation(request.getAnnotation());
		}

		if (request.getCategory() != null) {
			event.setCategory(getCategoryOrThrow(request.getCategory()));
		}

		if (request.getDescription() != null) {
			event.setDescription(request.getDescription());
		}

		if (request.getEventDate() != null) {
			eventValidationService.validateEventDateForUser(request.getEventDate());
			event.setEventDate(request.getEventDate());
		}

		if (request.getLocation() != null) {
			event.setLocation(EventMapper.toLocation(request.getLocation()));
		}

		if (request.getPaid() != null) {
			event.setPaid(request.getPaid());
		}

		if (request.getParticipantLimit() != null) {
			event.setParticipantLimit(request.getParticipantLimit());
		}

		if (request.getRequestModeration() != null) {
			event.setRequestModeration(request.getRequestModeration());
		}

		if (request.getTitle() != null) {
			event.setTitle(request.getTitle());
		}

		if (request.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
			event.setState(EventState.PENDING);
		}

		if (request.getStateAction() == UserStateAction.CANCEL_REVIEW) {
			event.setState(EventState.CANCELED);
		}
	}

	private User getUserOrThrow(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException(
						String.format("User with id=%d was not found", userId)
				));
	}

	private Category getCategoryOrThrow(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Category with id=%d was not found", categoryId)
				));
	}
}