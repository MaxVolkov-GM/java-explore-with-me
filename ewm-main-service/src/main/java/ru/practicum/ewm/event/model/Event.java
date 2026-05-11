package ru.practicum.ewm.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 2000)
	private String annotation;

	@Column(nullable = false, length = 7000)
	private String description;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(name = "event_date", nullable = false)
	private LocalDateTime eventDate;

	@Column(name = "created_on", nullable = false)
	private LocalDateTime createdOn;

	@Column(name = "published_on")
	private LocalDateTime publishedOn;

	@Column(nullable = false)
	private Boolean paid;

	@Column(name = "participant_limit", nullable = false)
	private Integer participantLimit;

	@Column(name = "request_moderation", nullable = false)
	private Boolean requestModeration;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EventState state;

	@Embedded
	private Location location;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "initiator_id", nullable = false)
	private User initiator;
}