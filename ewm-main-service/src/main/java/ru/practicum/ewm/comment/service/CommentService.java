package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final EventRepository eventRepository;

	@Transactional
	public CommentDto create(Long userId, Long eventId, NewCommentDto dto) {
		User author = getUserOrThrow(userId);
		Event event = getEventOrThrow(eventId);

		if (event.getState() != EventState.PUBLISHED) {
			throw new ConflictException("Only published events can be commented");
		}

		Comment comment = Comment.builder()
				.text(dto.getText())
				.author(author)
				.event(event)
				.createdOn(LocalDateTime.now())
				.build();

		Comment savedComment = commentRepository.save(comment);

		return CommentMapper.toDto(savedComment);
	}

	@Transactional
	public CommentDto update(Long userId, Long commentId, UpdateCommentDto dto) {
		getUserOrThrow(userId);

		Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Comment with id=%d was not found", commentId)
				));

		if (dto.getText() != null) {
			comment.setText(dto.getText());
			comment.setUpdatedOn(LocalDateTime.now());
		}

		Comment updatedComment = commentRepository.save(comment);

		return CommentMapper.toDto(updatedComment);
	}

	@Transactional
	public void deleteByAuthor(Long userId, Long commentId) {
		getUserOrThrow(userId);

		Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
				.orElseThrow(() -> new NotFoundException(
						String.format("Comment with id=%d was not found", commentId)
				));

		commentRepository.delete(comment);
	}

	@Transactional
	public void deleteByAdmin(Long commentId) {
		if (!commentRepository.existsById(commentId)) {
			throw new NotFoundException(String.format("Comment with id=%d was not found", commentId));
		}

		commentRepository.deleteById(commentId);
	}

	public List<CommentDto> getEventComments(Long eventId, int from, int size) {
		Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
				.orElseThrow(() -> new NotFoundException(
						String.format("Event with id=%d was not found", eventId)
				));

		Pageable pageable = new OffsetPageRequest(from, size);

		return commentRepository.findAllByEventId(event.getId(), pageable)
				.stream()
				.map(CommentMapper::toDto)
				.collect(Collectors.toList());
	}

	public List<CommentDto> getUserComments(Long userId, int from, int size) {
		getUserOrThrow(userId);

		Pageable pageable = new OffsetPageRequest(from, size);

		return commentRepository.findAllByAuthorId(userId, pageable)
				.stream()
				.map(CommentMapper::toDto)
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
}