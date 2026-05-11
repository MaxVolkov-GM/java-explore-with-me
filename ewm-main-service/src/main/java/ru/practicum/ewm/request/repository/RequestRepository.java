package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
	List<ParticipationRequest> findAllByRequesterId(Long requesterId);

	Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

	List<ParticipationRequest> findAllByEventIdAndEventInitiatorId(Long eventId, Long initiatorId);

	List<ParticipationRequest> findAllByIdInAndEventId(Collection<Long> requestIds, Long eventId);

	List<ParticipationRequest> findAllByEventIdAndStatusAndIdNotIn(Long eventId,
	                                                               RequestStatus status,
	                                                               Collection<Long> requestIds);

	boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

	long countByEventIdAndStatus(Long eventId, RequestStatus status);

	@Query("""
            SELECT r.event.id AS eventId, COUNT(r.id) AS confirmedRequests
            FROM ParticipationRequest r
            WHERE r.event.id IN :eventIds AND r.status = :status
            GROUP BY r.event.id
            """)
	List<EventConfirmedRequestsProjection> countConfirmedRequestsByEventIds(Collection<Long> eventIds,
	                                                                        RequestStatus status);
}