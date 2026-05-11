package ru.practicum.ewm.compilation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompilationServiceTest {
	@Mock
	private CompilationRepository compilationRepository;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private RequestRepository requestRepository;

	@InjectMocks
	private CompilationService compilationService;

	@Test
	void getCompilationShouldThrowWhenCompilationNotFound() {
		Long compilationId = 1L;

		when(compilationRepository.findById(compilationId)).thenReturn(Optional.empty());

		assertThrows(
				NotFoundException.class,
				() -> compilationService.getCompilation(compilationId)
		);
	}

	@Test
	void createShouldCreateCompilationWithEvents() {
		Long eventId = 10L;

		Event event = createEvent(eventId);

		NewCompilationDto dto = NewCompilationDto.builder()
				.title("Best events")
				.pinned(true)
				.events(Set.of(eventId))
				.build();

		when(eventRepository.findAllByIdIn(Set.of(eventId))).thenReturn(List.of(event));
		when(compilationRepository.save(any(Compilation.class))).thenAnswer(invocation -> {
			Compilation compilation = invocation.getArgument(0);
			compilation.setId(1L);
			return compilation;
		});
		when(requestRepository.countConfirmedRequestsByEventIds(List.of(eventId), RequestStatus.CONFIRMED))
				.thenReturn(List.of());

		CompilationDto result = compilationService.create(dto);

		assertEquals(1L, result.getId());
		assertEquals("Best events", result.getTitle());
		assertEquals(true, result.getPinned());
		assertEquals(1, result.getEvents().size());
	}

	private Event createEvent(Long id) {
		User initiator = new User();
		initiator.setId(1L);
		initiator.setName("Ivan Petrov");
		initiator.setEmail("ivan@example.com");

		Category category = new Category();
		category.setId(1L);
		category.setName("Concerts");

		Event event = new Event();
		event.setId(id);
		event.setAnnotation("This is a very interesting concert annotation");
		event.setCategory(category);
		event.setEventDate(LocalDateTime.now().plusDays(10));
		event.setInitiator(initiator);
		event.setPaid(true);
		event.setTitle("Test concert");

		return event;
	}
}