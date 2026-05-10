package ru.practicum.ewm.event.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.AdminStateAction;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.UserStateAction;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.EventConfirmedRequestsProjection;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
	private static final String APP_NAME = "ewm-main-service";
	private static final LocalDateTime STATS_START = LocalDateTime.of(2000, 1, 1, 0, 0);
	private static final Long DEFAULT_CONFIRMED_REQUESTS = 0L;
	private static final Long DEFAULT_VIEWS = 0L;

	private final EventRepository eventRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final RequestRepository requestRepository;
	private final StatsClient statsClient;

	@Transactional
	public EventFullDto create(Long userId, NewEventDto dto) {
		validateEventDateForUser(dto.getEventDate());

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
		Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

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

		Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

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
		Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

		return EventMapper.toFullDto(updatedEvent, confirmedRequests, DEFAULT_VIEWS);
	}

	public List<EventFullDto> getAdminEvents(List<Long> users,
	                                         List<EventState> states,
	                                         List<Long> categories,
	                                         LocalDateTime rangeStart,
	                                         LocalDateTime rangeEnd,
	                                         int from,
	                                         int size) {
		validateRange(rangeStart, rangeEnd);

		Pageable pageable = new OffsetPageRequest(from, size);
		Specification<Event> specification = buildAdminSpecification(users, states, categories, rangeStart, rangeEnd);
		List<Event> events = eventRepository.findAll(specification, pageable).getContent();
		Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

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
		Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

		return EventMapper.toFullDto(updatedEvent, confirmedRequests, DEFAULT_VIEWS);
	}

	public List<EventShortDto> getPublicEvents(String text,
	                                           List<Long> categories,
	                                           Boolean paid,
	                                           LocalDateTime rangeStart,
	                                           LocalDateTime rangeEnd,
	                                           Boolean onlyAvailable,
	                                           EventSort sort,
	                                           int from,
	                                           int size,
	                                           String uri,
	                                           String ip) {
		saveHit(uri, ip);
		validateRange(rangeStart, rangeEnd);

		Pageable pageable = createPublicPageable(from, size, sort);
		Specification<Event> specification = buildPublicSpecification(text, categories, paid, rangeStart, rangeEnd);

		List<Event> events = eventRepository.findAll(specification, pageable).getContent();
		Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

		if (Boolean.TRUE.equals(onlyAvailable)) {
			events = events.stream()
					.filter(event -> isAvailable(event, confirmedRequests.getOrDefault(
							event.getId(),
							DEFAULT_CONFIRMED_REQUESTS
					)))
					.collect(Collectors.toList());
		}

		Map<Long, Long> views = getViews(events);

		List<EventShortDto> result = events.stream()
				.map(event -> EventMapper.toShortDto(
						event,
						confirmedRequests.getOrDefault(event.getId(), DEFAULT_CONFIRMED_REQUESTS),
						views.getOrDefault(event.getId(), DEFAULT_VIEWS)
				))
				.collect(Collectors.toList());

		if (sort == EventSort.VIEWS) {
			result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
		}

		return result;
	}

	public EventFullDto getPublicEvent(Long eventId, String uri, String ip) {
		Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
				.orElseThrow(() -> new NotFoundException(
						String.format("Event with id=%d was not found", eventId)
				));

		saveHit(uri, ip);

		Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
		Long views = getViews(List.of(event)).getOrDefault(event.getId(), DEFAULT_VIEWS);

		return EventMapper.toFullDto(event, confirmedRequests, views);
	}

	private boolean isAvailable(Event event, Long confirmedRequests) {
		return event.getParticipantLimit() == 0 || confirmedRequests < event.getParticipantLimit();
	}

	private Pageable createPublicPageable(int from, int size, EventSort sort) {
		if (sort == EventSort.EVENT_DATE) {
			return new OffsetPageRequest(from, size, Sort.by(Sort.Direction.ASC, "eventDate"));
		}

		return new OffsetPageRequest(from, size);
	}

	private Specification<Event> buildPublicSpecification(String text,
	                                                      List<Long> categories,
	                                                      Boolean paid,
	                                                      LocalDateTime rangeStart,
	                                                      LocalDateTime rangeEnd) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

			if (text != null && !text.isBlank()) {
				String pattern = "%" + text.toLowerCase() + "%";
				Predicate annotationLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), pattern);
				Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
				predicates.add(criteriaBuilder.or(annotationLike, descriptionLike));
			}

			if (categories != null && !categories.isEmpty()) {
				predicates.add(root.get("category").get("id").in(categories));
			}

			if (paid != null) {
				predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
			}

			if (rangeStart == null && rangeEnd == null) {
				predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.now()));
			}

			if (rangeStart != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
			}

			if (rangeEnd != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}

	private Specification<Event> buildAdminSpecification(List<Long> users,
	                                                     List<EventState> states,
	                                                     List<Long> categories,
	                                                     LocalDateTime rangeStart,
	                                                     LocalDateTime rangeEnd) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (users != null && !users.isEmpty()) {
				predicates.add(root.get("initiator").get("id").in(users));
			}

			if (states != null && !states.isEmpty()) {
				predicates.add(root.get("state").in(states));
			}

			if (categories != null && !categories.isEmpty()) {
				predicates.add(root.get("category").get("id").in(categories));
			}

			if (rangeStart != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
			}

			if (rangeEnd != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}

	private Map<Long, Long> getConfirmedRequests(List<Event> events) {
		if (events == null || events.isEmpty()) {
			return Map.of();
		}

		List<Long> eventIds = events.stream()
				.map(Event::getId)
				.collect(Collectors.toList());

		return requestRepository.countConfirmedRequestsByEventIds(eventIds, RequestStatus.CONFIRMED)
				.stream()
				.collect(Collectors.toMap(
						EventConfirmedRequestsProjection::getEventId,
						EventConfirmedRequestsProjection::getConfirmedRequests
				));
	}

	private Map<Long, Long> getViews(List<Event> events) {
		if (events == null || events.isEmpty()) {
			return Map.of();
		}

		List<String> uris = events.stream()
				.map(event -> "/events/" + event.getId())
				.collect(Collectors.toList());

		List<ViewStatsDto> stats = statsClient.getStats(
				STATS_START,
				LocalDateTime.now().plusSeconds(1),
				uris,
				true
		);

		Map<Long, Long> views = new HashMap<>();

		if (stats == null) {
			return views;
		}

		for (ViewStatsDto viewStatsDto : stats) {
			Long eventId = extractEventIdFromUri(viewStatsDto.getUri());
			views.put(eventId, viewStatsDto.getHits());
		}

		return views;
	}

	private Long extractEventIdFromUri(String uri) {
		String[] parts = uri.split("/");
		return Long.parseLong(parts[parts.length - 1]);
	}

	private void saveHit(String uri, String ip) {
		EndpointHitDto hit = EndpointHitDto.builder()
				.app(APP_NAME)
				.uri(uri)
				.ip(ip)
				.timestamp(LocalDateTime.now())
				.build();

		statsClient.saveHit(hit);
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
			validateEventDateForUser(request.getEventDate());
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
			validateEventDateForAdmin(request.getEventDate());
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

		validateEventDateForAdmin(event.getEventDate());

		event.setState(EventState.PUBLISHED);
		event.setPublishedOn(LocalDateTime.now());
	}

	private void rejectEvent(Event event) {
		if (event.getState() == EventState.PUBLISHED) {
			throw new ConflictException("Cannot reject the event because it's already published");
		}

		event.setState(EventState.CANCELED);
	}

	private void validateEventDateForUser(LocalDateTime eventDate) {
		if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
			throw new ConflictException("Field: eventDate. Error: must be at least two hours after current time");
		}
	}

	private void validateEventDateForAdmin(LocalDateTime eventDate) {
		if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
			throw new ConflictException("Field: eventDate. Error: must be at least one hour after current time");
		}
	}

	private void validateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
		if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
			throw new IllegalArgumentException("rangeStart must be before rangeEnd");
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