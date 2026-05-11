package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.AdminStateAction;

import java.time.LocalDateTime;

import static ru.practicum.ewm.common.DateTimeConstants.DATE_TIME_PATTERN;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
	@Size(min = 20, max = 2000)
	private String annotation;

	private Long category;

	@Size(min = 20, max = 7000)
	private String description;

	@JsonFormat(pattern = DATE_TIME_PATTERN)
	private LocalDateTime eventDate;

	@Valid
	private LocationDto location;

	private Boolean paid;

	@PositiveOrZero
	private Integer participantLimit;

	private Boolean requestModeration;

	private AdminStateAction stateAction;

	@Size(min = 3, max = 120)
	private String title;
}