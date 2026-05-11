package ru.practicum.ewm.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventPrivateServiceTest {
	@Mock
	private EventRepository eventRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CategoryRepository categoryRepository;

	private EventPrivateService eventPrivateService;

	@BeforeEach
	void setUp() {
		eventPrivateService = new EventPrivateService(
				eventRepository,
				userRepository,
				categoryRepository,
				null,
				new EventValidationService()
		);
	}

	@Test
	void updateUserEventShouldThrowWhenEventIsPublished() {
		Long userId = 1L;
		Long eventId = 10L;

		User user = new User();
		user.setId(userId);
		user.setName("Ivan Petrov");
		user.setEmail("ivan@example.com");

		Event event = new Event();
		event.setId(eventId);
		event.setInitiator(user);
		event.setState(EventState.PUBLISHED);

		UpdateEventUserRequest request = new UpdateEventUserRequest();
		request.setTitle("Updated event title");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(eventRepository.findByIdAndInitiatorId(eventId, userId)).thenReturn(Optional.of(event));

		assertThrows(
				ConflictException.class,
				() -> eventPrivateService.updateUserEvent(userId, eventId, request)
		);
	}
}