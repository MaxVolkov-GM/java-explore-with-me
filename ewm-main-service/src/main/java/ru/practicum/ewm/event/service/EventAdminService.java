package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.AdminStateAction;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventAdminService {
	private static final Long DEFAULT_CONFIRMED_REQUESTS = 0L;
	private static final Long DEFAULT_VIEWS = 0L;

	private final EventRepository eventRepository;
	private final CategoryRepository categoryRepository;
	private final EventStatsService eventStatsService;
	private final EventQueryService eventQueryService;
	private final EventValidationService eventValidationService;

	public List<EventFullDto> getAdminEvents(List<Long> users,
	                                         List<EventState> states,
	                                         List<Long> categories,
	                                         LocalDateTime rangeStart,
	                                         LocalDateTime rangeEnd,
	                                         int from,
	                                         int size) {
		eventValidationService.validateRange(rangeStart, rangeEnd);

		Pageable pageable = new OffsetPageRequest(from, size);
		Specification<Event> specification = eventQueryService.buildAdminSpecification(
				users,
				states,
				categories,
				rangeStart,
				rangeEnd
		);

		List<Event> events = eventRepository.findAll(specification, pageable).getContent();
		Map<Long, Long> confirmedRequests = eventStatsService.getConfirmedRequests(events);

		return events.stream()
				.map(event -> EventMapper.toFullDto(
						event,
						confirmedRequests.getOrDefault(event.getId(), DEFAULT_CONFIRMED_REQUESTS),
						DEFAULT_VIEWS
				))
				.collect(Collectors.toList());
	}

	@Transactional
	public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Event with id=%d was not found", eventId)
				));

		applyAdminUpdate(event, request);

		Event updatedEvent = eventRepository.save(event);
		Long confirmedRequests = eventStatsService.getConfirmedRequests(eventId);

		return EventMapper.toFullDto(updatedEvent, confirmedRequests, DEFAULT_VIEWS);
	}

	private void applyAdminUpdate(Event event, UpdateEventAdminRequest request) {
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
			eventValidationService.validateEventDateForAdminUpdate(request.getEventDate());
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

		if (request.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
			publishEvent(event);
		}

		if (request.getStateAction() == AdminStateAction.REJECT_EVENT) {
			rejectEvent(event);
		}
	}

	private void publishEvent(Event event) {
		if (event.getState() != EventState.PENDING) {
			throw new ConflictException(
					String.format("Cannot publish the event because it's not in the right state: %s", event.getState())
			);
		}

		eventValidationService.validateEventDateForAdminPublication(event.getEventDate());

		event.setState(EventState.PUBLISHED);
		event.setPublishedOn(LocalDateTime.now());
	}

	private void rejectEvent(Event event) {
		if (event.getState() == EventState.PUBLISHED) {
			throw new ConflictException("Cannot reject the event because it's already published");
		}

		event.setState(EventState.CANCELED);
	}

	private Category getCategoryOrThrow(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Category with id=%d was not found", categoryId)
				));
	}
}