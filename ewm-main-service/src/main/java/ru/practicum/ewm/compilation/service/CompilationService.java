package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.EventConfirmedRequestsProjection;
import ru.practicum.ewm.request.repository.RequestRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {
	private final CompilationRepository compilationRepository;
	private final EventRepository eventRepository;
	private final RequestRepository requestRepository;

	@Transactional
	public CompilationDto create(NewCompilationDto dto) {
		Set<Event> events = getEvents(dto.getEvents());

		Compilation compilation = CompilationMapper.toEntity(dto, events);
		Compilation savedCompilation = compilationRepository.save(compilation);

		return CompilationMapper.toDto(savedCompilation, getConfirmedRequests(savedCompilation.getEvents()));
	}

	@Transactional
	public CompilationDto update(Long compilationId, UpdateCompilationRequest request) {
		Compilation compilation = getCompilationOrThrow(compilationId);

		if (request.getTitle() != null) {
			compilation.setTitle(request.getTitle());
		}

		if (request.getPinned() != null) {
			compilation.setPinned(request.getPinned());
		}

		if (request.getEvents() != null) {
			compilation.setEvents(getEvents(request.getEvents()));
		}

		Compilation updatedCompilation = compilationRepository.save(compilation);

		return CompilationMapper.toDto(updatedCompilation, getConfirmedRequests(updatedCompilation.getEvents()));
	}

	@Transactional
	public void delete(Long compilationId) {
		if (!compilationRepository.existsById(compilationId)) {
			throw new NotFoundException(String.format("Compilation with id=%d was not found", compilationId));
		}

		compilationRepository.deleteById(compilationId);
	}

	public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
		Pageable pageable = new OffsetPageRequest(from, size);

		List<Compilation> compilations = pinned == null
				? compilationRepository.findAll(pageable).getContent()
				: compilationRepository.findAllByPinned(pinned, pageable);

		Map<Long, Long> confirmedRequests = getConfirmedRequestsForCompilations(compilations);

		return compilations.stream()
				.map(compilation -> CompilationMapper.toDto(compilation, confirmedRequests))
				.collect(Collectors.toList());
	}

	public CompilationDto getCompilation(Long compilationId) {
		Compilation compilation = getCompilationOrThrow(compilationId);

		return CompilationMapper.toDto(compilation, getConfirmedRequests(compilation.getEvents()));
	}

	private Compilation getCompilationOrThrow(Long compilationId) {
		return compilationRepository.findById(compilationId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Compilation with id=%d was not found", compilationId)
				));
	}

	private Set<Event> getEvents(Set<Long> eventIds) {
		if (eventIds == null || eventIds.isEmpty()) {
			return new HashSet<>();
		}

		List<Event> events = eventRepository.findAllByIdIn(eventIds);

		if (events.size() != eventIds.size()) {
			throw new NotFoundException("One or more events were not found");
		}

		return new HashSet<>(events);
	}

	private Map<Long, Long> getConfirmedRequestsForCompilations(List<Compilation> compilations) {
		Set<Event> events = compilations.stream()
				.filter(compilation -> compilation.getEvents() != null)
				.flatMap(compilation -> compilation.getEvents().stream())
				.collect(Collectors.toSet());

		return getConfirmedRequests(events);
	}

	private Map<Long, Long> getConfirmedRequests(Set<Event> events) {
		if (events == null || events.isEmpty()) {
			return Map.of();
		}

		List<Long> eventIds = events.stream()
				.map(Event::getId)
				.collect(Collectors.toList());

		return requestRepository.countConfirmedRequestsByEventIds(eventIds, RequestStatus.CONFIRMED)
				.stream()
				.collect(Collectors.toMap(
						EventConfirmedRequestsProjection::getEventId,
						EventConfirmedRequestsProjection::getConfirmedRequests
				));
	}
}