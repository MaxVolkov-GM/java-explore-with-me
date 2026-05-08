package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {
	@Query("""
            SELECT new ru.practicum.stats.dto.ViewStatsDto(h.app, h.uri, COUNT(h.ip))
            FROM Hit h
            WHERE h.timestamp BETWEEN :start AND :end
            GROUP BY h.app, h.uri
            ORDER BY COUNT(h.ip) DESC
            """)
	List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end);

	@Query("""
            SELECT new ru.practicum.stats.dto.ViewStatsDto(h.app, h.uri, COUNT(h.ip))
            FROM Hit h
            WHERE h.timestamp BETWEEN :start AND :end
              AND h.uri IN :uris
            GROUP BY h.app, h.uri
            ORDER BY COUNT(h.ip) DESC
            """)
	List<ViewStatsDto> getStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

	@Query("""
            SELECT new ru.practicum.stats.dto.ViewStatsDto(h.app, h.uri, COUNT(DISTINCT h.ip))
            FROM Hit h
            WHERE h.timestamp BETWEEN :start AND :end
            GROUP BY h.app, h.uri
            ORDER BY COUNT(DISTINCT h.ip) DESC
            """)
	List<ViewStatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end);

	@Query("""
            SELECT new ru.practicum.stats.dto.ViewStatsDto(h.app, h.uri, COUNT(DISTINCT h.ip))
            FROM Hit h
            WHERE h.timestamp BETWEEN :start AND :end
              AND h.uri IN :uris
            GROUP BY h.app, h.uri
            ORDER BY COUNT(DISTINCT h.ip) DESC
            """)
	List<ViewStatsDto> getUniqueStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}