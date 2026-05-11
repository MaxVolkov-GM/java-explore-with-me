package ru.practicum.ewm.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {
	@Mock
	private RequestRepository requestRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private EventRepository eventRepository;

	@InjectMocks
	private RequestService requestService;

	@Test
	void createShouldThrowWhenRequesterIsEventInitiator() {
		Long userId = 1L;
		Long eventId = 10L;

		User user = createUser(userId);
		Event event = createEvent(eventId, user, EventState.PUBLISHED, 10, true);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

		assertThrows(
				ConflictException.class,
				() -> requestService.create(userId, eventId)
		);
	}

	@Test
	void createShouldThrowWhenRequestAlreadyExists() {
		Long requesterId = 2L;
		Long initiatorId = 1L;
		Long eventId = 10L;

		User requester = createUser(requesterId);
		User initiator = createUser(initiatorId);
		Event event = createEvent(eventId, initiator, EventState.PUBLISHED, 10, true);

		when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		when(requestRepository.existsByRequesterIdAndEventId(requesterId, eventId)).thenReturn(true);

		assertThrows(
				ConflictException.class,
				() -> requestService.create(requesterId, eventId)
		);
	}

	@Test
	void createShouldThrowWhenEventIsNotPublished() {
		Long requesterId = 2L;
		Long initiatorId = 1L;
		Long eventId = 10L;

		User requester = createUser(requesterId);
		User initiator = createUser(initiatorId);
		Event event = createEvent(eventId, initiator, EventState.PENDING, 10, true);

		when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		when(requestRepository.existsByRequesterIdAndEventId(requesterId, eventId)).thenReturn(false);

		assertThrows(
				ConflictException.class,
				() -> requestService.create(requesterId, eventId)
		);
	}

	@Test
	void createShouldCreateConfirmedRequestWhenModerationDisabled() {
		Long requesterId = 2L;
		Long initiatorId = 1L;
		Long eventId = 10L;

		User requester = createUser(requesterId);
		User initiator = createUser(initiatorId);
		Event event = createEvent(eventId, initiator, EventState.PUBLISHED, 10, false);

		when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
		when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
		when(requestRepository.existsByRequesterIdAndEventId(requesterId, eventId)).thenReturn(false);
		when(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)).thenReturn(0L);
		when(requestRepository.save(any(ParticipationRequest.class))).thenAnswer(invocation -> {
			ParticipationRequest request = invocation.getArgument(0);
			request.setId(100L);
			return request;
		});

		ParticipationRequestDto result = requestService.create(requesterId, eventId);

		assertEquals(100L, result.getId());
		assertEquals(eventId, result.getEvent());
		assertEquals(requesterId, result.getRequester());
		assertEquals(RequestStatus.CONFIRMED, result.getStatus());
	}

	private User createUser(Long id) {
		User user = new User();
		user.setId(id);
		user.setName("User " + id);
		user.setEmail("user" + id + "@example.com");
		return user;
	}

	private Event createEvent(Long id, User initiator, EventState state, Integer participantLimit, Boolean requestModeration) {
		Event event = new Event();
		event.setId(id);
		event.setInitiator(initiator);
		event.setState(state);
		event.setParticipantLimit(participantLimit);
		event.setRequestModeration(requestModeration);
		return event;
	}
}