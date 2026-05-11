package ru.practicum.ewm.event.service;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.exception.ConflictException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventValidationServiceTest {
	private final EventValidationService eventValidationService = new EventValidationService();

	@Test
	void validateEventDateForUserShouldThrowWhenDateIsTooEarly() {
		LocalDateTime eventDate = LocalDateTime.now().plusHours(1);

		assertThrows(
				IllegalArgumentException.class,
				() -> eventValidationService.validateEventDateForUser(eventDate)
		);
	}

	@Test
	void validateEventDateForUserShouldNotThrowWhenDateIsValid() {
		LocalDateTime eventDate = LocalDateTime.now().plusHours(3);

		assertDoesNotThrow(() -> eventValidationService.validateEventDateForUser(eventDate));
	}

	@Test
	void validateEventDateForAdminUpdateShouldThrowWhenDateIsTooEarly() {
		LocalDateTime eventDate = LocalDateTime.now().plusMinutes(30);

		assertThrows(
				IllegalArgumentException.class,
				() -> eventValidationService.validateEventDateForAdminUpdate(eventDate)
		);
	}

	@Test
	void validateEventDateForAdminPublicationShouldThrowConflictWhenDateIsTooEarly() {
		LocalDateTime eventDate = LocalDateTime.now().plusMinutes(30);

		assertThrows(
				ConflictException.class,
				() -> eventValidationService.validateEventDateForAdminPublication(eventDate)
		);
	}

	@Test
	void validateRangeShouldThrowWhenStartAfterEnd() {
		LocalDateTime start = LocalDateTime.now().plusDays(2);
		LocalDateTime end = LocalDateTime.now().plusDays(1);

		assertThrows(
				IllegalArgumentException.class,
				() -> eventValidationService.validateRange(start, end)
		);
	}
}