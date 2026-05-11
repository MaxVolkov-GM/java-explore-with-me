package ru.practicum.ewm.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {
	public Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
		return Compilation.builder()
				.title(dto.getTitle())
				.pinned(Boolean.TRUE.equals(dto.getPinned()))
				.events(events)
				.build();
	}

	public CompilationDto toDto(Compilation compilation, Map<Long, Long> confirmedRequests) {
		Set<EventShortDto> events = compilation.getEvents() == null
				? Collections.emptySet()
				: compilation.getEvents()
				.stream()
				.map(event -> EventMapper.toShortDto(
						event,
						confirmedRequests.getOrDefault(event.getId(), 0L),
						0L
				))
				.collect(Collectors.toSet());

		return CompilationDto.builder()
				.id(compilation.getId())
				.title(compilation.getTitle())
				.pinned(compilation.getPinned())
				.events(events)
				.build();
	}
}