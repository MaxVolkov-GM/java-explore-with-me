package ru.practicum.ewm.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;

@UtilityClass
public class ParticipationRequestMapper {
	public ParticipationRequestDto toDto(ParticipationRequest request) {
		return ParticipationRequestDto.builder()
				.id(request.getId())
				.created(request.getCreated())
				.event(request.getEvent().getId())
				.requester(request.getRequester().getId())
				.status(request.getStatus())
				.build();
	}
}