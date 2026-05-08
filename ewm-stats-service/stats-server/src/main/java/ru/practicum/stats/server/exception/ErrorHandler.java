package ru.practicum.stats.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {
	private static final DateTimeFormatter FORMATTER =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handleBadRequest(BadRequestException exception) {
		return Map.of(
				"status", HttpStatus.BAD_REQUEST.name(),
				"reason", "Incorrectly made request.",
				"message", exception.getMessage(),
				"timestamp", LocalDateTime.now().format(FORMATTER)
		);
	}
}