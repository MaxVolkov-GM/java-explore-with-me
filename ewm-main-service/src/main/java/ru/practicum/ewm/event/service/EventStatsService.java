package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.EventConfirmedRequestsProjection;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventStatsService {
	private static final String APP_NAME = "ewm-main-service";
	private static final LocalDateTime STATS_START = LocalDateTime.of(2000, 1, 1, 0, 0);

	private final RequestRepository requestRepository;
	private final StatsClient statsClient;

	public void saveHit(String uri, String ip) {
		EndpointHitDto hit = EndpointHitDto.builder()
				.app(APP_NAME)
				.uri(uri)
				.ip(ip)
				.timestamp(LocalDateTime.now())
				.build();

		statsClient.saveHit(hit);
	}

	public Map<Long, Long> getViews(List<Event> events) {
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

	public Map<Long, Long> getConfirmedRequests(List<Event> events) {
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

	public Long getConfirmedRequests(Long eventId) {
		return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
	}

	private Long extractEventIdFromUri(String uri) {
		String[] parts = uri.split("/");
		return Long.parseLong(parts[parts.length - 1]);
	}
}