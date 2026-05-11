package ru.practicum.ewm.event.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.ConflictException;

import java.time.LocalDateTime;

@Service
public class EventValidationService {
	public void validateEventDateForUser(LocalDateTime eventDate) {
		if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
			throw new IllegalArgumentException("Field: eventDate. Error: must be at least two hours after current time");
		}
	}

	public void validateEventDateForAdminUpdate(LocalDateTime eventDate) {
		if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
			throw new IllegalArgumentException("Field: eventDate. Error: must be at least one hour after current time");
		}
	}

	public void validateEventDateForAdminPublication(LocalDateTime eventDate) {
		if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
			throw new ConflictException("Field: eventDate. Error: must be at least one hour after current time");
		}
	}

	public void validateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
		if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
			throw new IllegalArgumentException("rangeStart must be before rangeEnd");
		}
	}
}