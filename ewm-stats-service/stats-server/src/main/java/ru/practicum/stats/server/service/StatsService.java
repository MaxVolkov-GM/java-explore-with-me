package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.exception.BadRequestException;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.model.Hit;
import ru.practicum.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {
	private final HitRepository hitRepository;

	@Transactional
	public EndpointHitDto saveHit(EndpointHitDto dto) {
		Hit hit = HitMapper.toEntity(dto);
		Hit saved = hitRepository.save(hit);
		return HitMapper.toDto(saved);
	}

	public List<ViewStatsDto> getStats(LocalDateTime start,
	                                   LocalDateTime end,
	                                   List<String> uris,
	                                   Boolean unique) {
		if (start.isAfter(end)) {
			throw new BadRequestException("Start date must be before end date");
		}

		boolean onlyUnique = Boolean.TRUE.equals(unique);
		boolean withoutUris = uris == null || uris.isEmpty();

		if (onlyUnique && withoutUris) {
			return hitRepository.getUniqueStats(start, end);
		}

		if (onlyUnique) {
			return hitRepository.getUniqueStatsByUris(start, end, uris);
		}

		if (withoutUris) {
			return hitRepository.getStats(start, end);
		}

		return hitRepository.getStatsByUris(start, end, uris);
	}
}