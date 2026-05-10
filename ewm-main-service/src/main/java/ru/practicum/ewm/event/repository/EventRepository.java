package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
	@EntityGraph(attributePaths = {"category", "initiator"})
	List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

	@EntityGraph(attributePaths = {"category", "initiator"})
	Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

	@EntityGraph(attributePaths = {"category", "initiator"})
	Optional<Event> findByIdAndState(Long eventId, EventState state);

	@EntityGraph(attributePaths = {"category", "initiator"})
	List<Event> findAllByIdIn(Collection<Long> ids);

	@Override
	@EntityGraph(attributePaths = {"category", "initiator"})
	Page<Event> findAll(Specification<Event> specification, Pageable pageable);
}