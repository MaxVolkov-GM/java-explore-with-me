package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventPrivateService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
	private final EventPrivateService eventPrivateService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EventFullDto create(@PathVariable Long userId,
	                           @Valid @RequestBody NewEventDto dto) {
		return eventPrivateService.create(userId, dto);
	}

	@GetMapping
	public List<EventShortDto> getUserEvents(@PathVariable Long userId,
	                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
	                                         @RequestParam(defaultValue = "10") @Positive int size) {
		return eventPrivateService.getUserEvents(userId, from, size);
	}

	@GetMapping("/{eventId}")
	public EventFullDto getUserEvent(@PathVariable Long userId,
	                                 @PathVariable Long eventId) {
		return eventPrivateService.getUserEvent(userId, eventId);
	}

	@PatchMapping("/{eventId}")
	public EventFullDto updateUserEvent(@PathVariable Long userId,
	                                    @PathVariable Long eventId,
	                                    @Valid @RequestBody UpdateEventUserRequest request) {
		return eventPrivateService.updateUserEvent(userId, eventId, request);
	}
}