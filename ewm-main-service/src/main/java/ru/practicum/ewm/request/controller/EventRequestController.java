package ru.practicum.ewm.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class EventRequestController {
	private final RequestService requestService;

	@GetMapping
	public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
	                                                      @PathVariable Long eventId) {
		return requestService.getEventRequests(userId, eventId);
	}

	@PatchMapping
	public EventRequestStatusUpdateResult updateEventRequests(@PathVariable Long userId,
	                                                          @PathVariable Long eventId,
	                                                          @Valid @RequestBody EventRequestStatusUpdateRequest request) {
		return requestService.updateEventRequests(userId, eventId, request);
	}
}