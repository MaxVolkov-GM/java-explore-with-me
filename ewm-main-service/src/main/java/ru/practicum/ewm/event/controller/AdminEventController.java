package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.service.EventAdminService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.common.DateTimeConstants.DATE_TIME_FORMATTER;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {
	private final EventAdminService eventAdminService;

	@GetMapping
	public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
	                                    @RequestParam(required = false) List<EventState> states,
	                                    @RequestParam(required = false) List<Long> categories,
	                                    @RequestParam(required = false) String rangeStart,
	                                    @RequestParam(required = false) String rangeEnd,
	                                    @RequestParam(defaultValue = "0") @PositiveOrZero int from,
	                                    @RequestParam(defaultValue = "10") @Positive int size) {
		return eventAdminService.getAdminEvents(
				users,
				states,
				categories,
				parseDate(rangeStart),
				parseDate(rangeEnd),
				from,
				size
		);
	}

	@PatchMapping("/{eventId}")
	public EventFullDto updateEvent(@PathVariable Long eventId,
	                                @Valid @RequestBody UpdateEventAdminRequest request) {
		return eventAdminService.updateAdminEvent(eventId, request);
	}

	private LocalDateTime parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
	}
}