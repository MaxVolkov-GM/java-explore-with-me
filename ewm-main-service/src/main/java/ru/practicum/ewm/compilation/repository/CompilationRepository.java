package ru.practicum.ewm.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
	@Override
	@EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
	Page<Compilation> findAll(Pageable pageable);

	@EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
	List<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

	@Override
	@EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
	Optional<Compilation> findById(Long id);
}