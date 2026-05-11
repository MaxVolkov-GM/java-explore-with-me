package ru.practicum.ewm.request.repository;

public interface EventConfirmedRequestsProjection {
	Long getEventId();

	Long getConfirmedRequests();
}