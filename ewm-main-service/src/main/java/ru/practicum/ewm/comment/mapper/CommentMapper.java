package ru.practicum.ewm.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.model.Comment;

@UtilityClass
public class CommentMapper {
	public CommentDto toDto(Comment comment) {
		return CommentDto.builder()
				.id(comment.getId())
				.text(comment.getText())
				.eventId(comment.getEvent().getId())
				.authorId(comment.getAuthor().getId())
				.authorName(comment.getAuthor().getName())
				.createdOn(comment.getCreatedOn())
				.updatedOn(comment.getUpdatedOn())
				.build();
	}
}