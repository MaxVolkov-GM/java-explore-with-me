package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
		List<String> errors = exception.getFieldErrors()
				.stream()
				.map(this::formatFieldError)
				.collect(Collectors.toList());

		return ApiError.builder()
				.errors(errors)
				.message(String.join("; ", errors))
				.reason("Incorrectly made request.")
				.status(HttpStatus.BAD_REQUEST.name())
				.timestamp(LocalDateTime.now().format(FORMATTER))
				.build();
	}

	@ExceptionHandler({
			ConstraintViolationException.class,
			MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class,
			DateTimeParseException.class,
			IllegalArgumentException.class
	})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleBadRequest(Exception exception) {
		return ApiError.builder()
				.errors(List.of(exception.getMessage()))
				.message(exception.getMessage())
				.reason("Incorrectly made request.")
				.status(HttpStatus.BAD_REQUEST.name())
				.timestamp(LocalDateTime.now().format(FORMATTER))
				.build();
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleNotFound(NotFoundException exception) {
		return ApiError.builder()
				.errors(List.of(exception.getMessage()))
				.message(exception.getMessage())
				.reason("The required object was not found.")
				.status(HttpStatus.NOT_FOUND.name())
				.timestamp(LocalDateTime.now().format(FORMATTER))
				.build();
	}

	@ExceptionHandler(ConflictException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiError handleConflict(ConflictException exception) {
		return ApiError.builder()
				.errors(List.of(exception.getMessage()))
				.message(exception.getMessage())
				.reason("For the requested operation the conditions are not met.")
				.status(HttpStatus.CONFLICT.name())
				.timestamp(LocalDateTime.now().format(FORMATTER))
				.build();
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiError handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		return ApiError.builder()
				.errors(List.of(exception.getMessage()))
				.message(exception.getMessage())
				.reason("Integrity constraint has been violated.")
				.status(HttpStatus.CONFLICT.name())
				.timestamp(LocalDateTime.now().format(FORMATTER))
				.build();
	}

	private String formatFieldError(FieldError error) {
		return String.format(
				"Field: %s. Error: %s. Value: %s",
				error.getField(),
				error.getDefaultMessage(),
				error.getRejectedValue()
		);
	}
}