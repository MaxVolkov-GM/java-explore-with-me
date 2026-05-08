package ru.practicum.stats.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
	private static final DateTimeFormatter FORMATTER =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final StatsService statsService;

	@PostMapping("/hit")
	@ResponseStatus(HttpStatus.CREATED)
	public EndpointHitDto saveHit(@RequestBody EndpointHitDto endpointHitDto) {
		return statsService.saveHit(endpointHitDto);
	}

	@GetMapping("/stats")
	public List<ViewStatsDto> getStats(@RequestParam String start,
	                                   @RequestParam String end,
	                                   @RequestParam(required = false) List<String> uris,
	                                   @RequestParam(defaultValue = "false") Boolean unique) {
		LocalDateTime startDate = LocalDateTime.parse(start, FORMATTER);
		LocalDateTime endDate = LocalDateTime.parse(end, FORMATTER);

		return statsService.getStats(startDate, endDate, uris, unique);
	}
}