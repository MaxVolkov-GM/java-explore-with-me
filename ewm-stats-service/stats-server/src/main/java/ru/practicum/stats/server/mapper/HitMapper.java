package ru.practicum.stats.server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.server.model.Hit;

@UtilityClass
public class HitMapper {
	public Hit toEntity(EndpointHitDto dto) {
		return Hit.builder()
				.id(dto.getId())
				.app(dto.getApp())
				.uri(dto.getUri())
				.ip(dto.getIp())
				.timestamp(dto.getTimestamp())
				.build();
	}

	public EndpointHitDto toDto(Hit hit) {
		return EndpointHitDto.builder()
				.id(hit.getId())
				.app(hit.getApp())
				.uri(hit.getUri())
				.ip(hit.getIp())
				.timestamp(hit.getTimestamp())
				.build();
	}
}