package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.model.RequestUpdateStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {
	private final RequestRepository requestRepository;
	private final UserRepository userRepository;
	private final EventRepository eventRepository;

	public List<ParticipationRequestDto> getUserRequests(Long userId) {
		getUserOrThrow(userId);

		return requestRepository.findAllByRequesterId(userId)
				.stream()
				.map(ParticipationRequestMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public ParticipationRequestDto create(Long userId, Long eventId) {
		User requester = getUserOrThrow(userId);
		Event event = getEventOrThrow(eventId);

		if (event.getInitiator().getId().equals(userId)) {
			throw new ConflictException("The initiator of the event cannot add a request to participate in his event");
		}

		if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
			throw new ConflictException("Request already exists");
		}

		if (event.getState() != EventState.PUBLISHED) {
			throw new ConflictException("Cannot participate in unpublished event");
		}

		long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

		if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
			throw new ConflictException("The participant limit has been reached");
		}

		RequestStatus status = defineInitialStatus(event);

		ParticipationRequest request = ParticipationRequest.builder()
				.created(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
				.event(event)
				.requester(requester)
				.status(status)
				.build();

		ParticipationRequest savedRequest = requestRepository.save(request);

		return ParticipationRequestMapper.toDto(savedRequest);
	}

	@Transactional
	public ParticipationRequestDto cancel(Long userId, Long requestId) {
		getUserOrThrow(userId);

		ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Request with id=%d was not found", requestId)
				));

		request.setStatus(RequestStatus.CANCELED);

		ParticipationRequest savedRequest = requestRepository.save(request);

		return ParticipationRequestMapper.toDto(savedRequest);
	}

	public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
		getUserOrThrow(userId);
		getEventByInitiatorOrThrow(userId, eventId);

		return requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId)
				.stream()
				.map(ParticipationRequestMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public EventRequestStatusUpdateResult updateEventRequests(Long userId,
	                                                          Long eventId,
	                                                          EventRequestStatusUpdateRequest updateRequest) {
		getUserOrThrow(userId);

		Event event = getEventByInitiatorOrThrow(userId, eventId);

		List<ParticipationRequest> requests = requestRepository.findAllByIdInAndEventId(
				updateRequest.getRequestIds(),
				eventId
		);

		if (requests.size() != updateRequest.getRequestIds().size()) {
			throw new NotFoundException("One or more requests were not found");
		}

		for (ParticipationRequest request : requests) {
			if (request.getStatus() != RequestStatus.PENDING) {
				throw new ConflictException("Request must have status PENDING");
			}
		}

		if (updateRequest.getStatus() == RequestUpdateStatus.REJECTED) {
			return rejectRequests(requests);
		}

		return confirmRequests(event, requests);
	}

	private EventRequestStatusUpdateResult confirmRequests(Event event, List<ParticipationRequest> requests) {
		long confirmedRequestsCount = requestRepository.countByEventIdAndStatus(
				event.getId(),
				RequestStatus.CONFIRMED
		);

		if (event.getParticipantLimit() > 0
				&& confirmedRequestsCount + requests.size() > event.getParticipantLimit()) {
			throw new ConflictException("The participant limit has been reached");
		}

		for (ParticipationRequest request : requests) {
			request.setStatus(RequestStatus.CONFIRMED);
		}

		List<ParticipationRequest> confirmedRequests = requestRepository.saveAll(requests);
		List<ParticipationRequest> rejectedRequests = new ArrayList<>();

		if (event.getParticipantLimit() > 0
				&& confirmedRequestsCount + confirmedRequests.size() == event.getParticipantLimit()) {
			rejectedRequests = requestRepository.findAllByEventIdAndStatusAndIdNotIn(
					event.getId(),
					RequestStatus.PENDING,
					confirmedRequests.stream()
							.map(ParticipationRequest::getId)
							.collect(Collectors.toList())
			);

			for (ParticipationRequest request : rejectedRequests) {
				request.setStatus(RequestStatus.REJECTED);
			}

			rejectedRequests = requestRepository.saveAll(rejectedRequests);
		}

		return EventRequestStatusUpdateResult.builder()
				.confirmedRequests(toDtoList(confirmedRequests))
				.rejectedRequests(toDtoList(rejectedRequests))
				.build();
	}

	private EventRequestStatusUpdateResult rejectRequests(List<ParticipationRequest> requests) {
		for (ParticipationRequest request : requests) {
			request.setStatus(RequestStatus.REJECTED);
		}

		List<ParticipationRequest> rejectedRequests = requestRepository.saveAll(requests);

		return EventRequestStatusUpdateResult.builder()
				.confirmedRequests(List.of())
				.rejectedRequests(toDtoList(rejectedRequests))
				.build();
	}

	private RequestStatus defineInitialStatus(Event event) {
		if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
			return RequestStatus.CONFIRMED;
		}

		return RequestStatus.PENDING;
	}

	private List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests) {
		return requests.stream()
				.map(ParticipationRequestMapper::toDto)
				.collect(Collectors.toList());
	}

	private User getUserOrThrow(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException(
						String.format("User with id=%d was not found", userId)
				));
	}

	private Event getEventOrThrow(Long eventId) {
		return eventRepository.findById(eventId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Event with id=%d was not found", eventId)
				));
	}

	private Event getEventByInitiatorOrThrow(Long userId, Long eventId) {
		return eventRepository.findByIdAndInitiatorId(eventId, userId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Event with id=%d was not found", eventId)
				));
	}
}