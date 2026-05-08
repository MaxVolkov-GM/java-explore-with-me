package ru.practicum.stats.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatsClient {
	private static final DateTimeFormatter FORMATTER =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final RestTemplate restTemplate;
	private final String serverUrl;

	public StatsClient(String serverUrl) {
		this.restTemplate = new RestTemplate();
		this.serverUrl = serverUrl;
	}

	public void saveHit(EndpointHitDto endpointHitDto) {
		restTemplate.postForEntity(
				serverUrl + "/hit",
				endpointHitDto,
				EndpointHitDto.class
		);
	}

	public List<ViewStatsDto> getStats(LocalDateTime start,
	                                   LocalDateTime end,
	                                   List<String> uris,
	                                   Boolean unique) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(serverUrl + "/stats")
				.queryParam("start", start.format(FORMATTER))
				.queryParam("end", end.format(FORMATTER))
				.queryParam("unique", unique);

		if (uris != null) {
			for (String uri : uris) {
				builder.queryParam("uris", uri);
			}
		}

		ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
				builder.encode().toUriString(),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				}
		);

		return response.getBody();
	}
}