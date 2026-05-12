package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comment.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	@EntityGraph(attributePaths = {"event", "author"})
	List<Comment> findAllByEventId(Long eventId, Pageable pageable);

	@EntityGraph(attributePaths = {"event", "author"})
	List<Comment> findAllByAuthorId(Long authorId, Pageable pageable);

	@EntityGraph(attributePaths = {"event", "author"})
	Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

	@Override
	@EntityGraph(attributePaths = {"event", "author"})
	Optional<Comment> findById(Long commentId);
}