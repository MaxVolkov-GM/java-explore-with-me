package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPublicService {
	private static final Long DEFAULT_CONFIRMED_REQUESTS = 0L;
	private static final Long DEFAULT_VIEWS = 0L;

	private final EventRepository eventRepository;
	private final EventStatsService eventStatsService;
	private final EventQueryService eventQueryService;
	private final EventValidationService eventValidationService;

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
		eventStatsService.saveHit(uri, ip);
		eventValidationService.validateRange(rangeStart, rangeEnd);

		Pageable pageable = eventQueryService.createPublicPageable(from, size, sort);
		Specification<Event> specification = eventQueryService.buildPublicSpecification(
				text,
				categories,
				paid,
				rangeStart,
				rangeEnd
		);

		List<Event> events = eventRepository.findAll(specification, pageable).getContent();
		Map<Long, Long> confirmedRequests = eventStatsService.getConfirmedRequests(events);

		if (Boolean.TRUE.equals(onlyAvailable)) {
			events = events.stream()
					.filter(event -> isAvailable(event, confirmedRequests.getOrDefault(
							event.getId(),
							DEFAULT_CONFIRMED_REQUESTS
					)))
					.collect(Collectors.toList());
		}

		Map<Long, Long> views = eventStatsService.getViews(events);

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

		eventStatsService.saveHit(uri, ip);

		Long confirmedRequests = eventStatsService.getConfirmedRequests(eventId);
		Long views = eventStatsService.getViews(List.of(event)).getOrDefault(event.getId(), DEFAULT_VIEWS);

		return EventMapper.toFullDto(event, confirmedRequests, views);
	}

	private boolean isAvailable(Event event, Long confirmedRequests) {
		return event.getParticipantLimit() == 0 || confirmedRequests < event.getParticipantLimit();
	}
}