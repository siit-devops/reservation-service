package com.devops.reservation_service.repository;

import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.model.enumerations.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    @Query("""
            select r from Reservation r
            where (r.userId = ?1 or r.hostId = ?2) and
            r.reservationStatus in ?3""")
    List<Reservation> findAllByUserIdOrHostIdAAndReservationStatus(UUID userId, UUID hostId, List<ReservationStatus> reservationStatuses);
}
