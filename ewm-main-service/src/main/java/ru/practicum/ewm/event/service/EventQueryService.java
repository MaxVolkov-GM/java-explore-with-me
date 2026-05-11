package ru.practicum.ewm.event.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.common.OffsetPageRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventQueryService {
	public Pageable createPublicPageable(int from, int size, EventSort sort) {
		if (sort == EventSort.EVENT_DATE) {
			return new OffsetPageRequest(from, size, Sort.by(Sort.Direction.ASC, "eventDate"));
		}

		return new OffsetPageRequest(from, size);
	}

	public Specification<Event> buildPublicSpecification(String text,
	                                                     List<Long> categories,
	                                                     Boolean paid,
	                                                     LocalDateTime rangeStart,
	                                                     LocalDateTime rangeEnd) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

			if (text != null && !text.isBlank()) {
				String pattern = "%" + text.toLowerCase() + "%";
				Predicate annotationLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), pattern);
				Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
				predicates.add(criteriaBuilder.or(annotationLike, descriptionLike));
			}

			if (categories != null && !categories.isEmpty()) {
				predicates.add(root.get("category").get("id").in(categories));
			}

			if (paid != null) {
				predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
			}

			if (rangeStart == null && rangeEnd == null) {
				predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.now()));
			}

			if (rangeStart != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
			}

			if (rangeEnd != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}

	public Specification<Event> buildAdminSpecification(List<Long> users,
	                                                    List<EventState> states,
	                                                    List<Long> categories,
	                                                    LocalDateTime rangeStart,
	                                                    LocalDateTime rangeEnd) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (users != null && !users.isEmpty()) {
				predicates.add(root.get("initiator").get("id").in(users));
			}

			if (states != null && !states.isEmpty()) {
				predicates.add(root.get("state").in(states));
			}

			if (categories != null && !categories.isEmpty()) {
				predicates.add(root.get("category").get("id").in(categories));
			}

			if (rangeStart != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
			}

			if (rangeEnd != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}