package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.service.EventPublicService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final EventPublicService eventPublicService;

	@GetMapping
	public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
	                                     @RequestParam(required = false) List<Long> categories,
	                                     @RequestParam(required = false) Boolean paid,
	                                     @RequestParam(required = false) String rangeStart,
	                                     @RequestParam(required = false) String rangeEnd,
	                                     @RequestParam(defaultValue = "false") Boolean onlyAvailable,
	                                     @RequestParam(required = false) EventSort sort,
	                                     @RequestParam(defaultValue = "0") @PositiveOrZero int from,
	                                     @RequestParam(defaultValue = "10") @Positive int size,
	                                     HttpServletRequest request) {
		return eventPublicService.getPublicEvents(
				text,
				categories,
				paid,
				parseDate(rangeStart),
				parseDate(rangeEnd),
				onlyAvailable,
				sort,
				from,
				size,
				request.getRequestURI(),
				request.getRemoteAddr()
		);
	}

	@GetMapping("/{id}")
	public EventFullDto getEvent(@PathVariable Long id,
	                             HttpServletRequest request) {
		return eventPublicService.getPublicEvent(
				id,
				request.getRequestURI(),
				request.getRemoteAddr()
		);
	}

	private LocalDateTime parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return LocalDateTime.parse(value, FORMATTER);
	}
}