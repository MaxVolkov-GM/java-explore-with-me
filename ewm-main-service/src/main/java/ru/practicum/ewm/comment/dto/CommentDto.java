package ru.practicum.ewm.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static ru.practicum.ewm.common.DateTimeConstants.DATE_TIME_PATTERN;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
	private Long id;
	private String text;
	private Long eventId;
	private Long authorId;
	private String authorName;

	@JsonFormat(pattern = DATE_TIME_PATTERN)
	private LocalDateTime createdOn;

	@JsonFormat(pattern = DATE_TIME_PATTERN)
	private LocalDateTime updatedOn;
}