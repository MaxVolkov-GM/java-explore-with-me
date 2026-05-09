package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
	@EntityGraph(attributePaths = {"category", "initiator"})
	List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

	@EntityGraph(attributePaths = {"category", "initiator"})
	Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);
}