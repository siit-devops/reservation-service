package com.devops.reservation_service.repository;

import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.model.enumerations.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatusCode;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    @Query("""
            select r from Reservation r
            where (r.userId = ?1 or r.hostId = ?2) and
            r.reservationStatus in ?3""")
    List<Reservation> findAllByUserIdOrHostIdAAndReservationStatus(UUID userId, UUID hostId, List<ReservationStatus> reservationStatuses);

    @Query("""
            select r from Reservation r
            where r.userId = ?1 and r.hostId = ?2 and
            r.reservationStatus in ?3""")
    List<Reservation> findAllByUserIdAndHostIdAAndReservationStatus(UUID userId, UUID hostId, List<ReservationStatus> reservationStatuses);

    @Query("""
            select r from Reservation r
            where r.userId = ?1 and r.accommodationId = ?2 and
            r.reservationStatus in ?3""")
    List<Reservation> findAllByUserIdAndAccomodationIdAAndReservationStatus(UUID userId, UUID accomodationId, List<ReservationStatus> reservationStatuses);

    @Query("""
            select r from Reservation r
            where
            r.accommodationId = ?1 and
            ((r.startDate between ?2 and ?3) or
            (r.endDate between ?2 and ?3) or
            (?2 between r.startDate and r.endDate) or
            (?3 between r.startDate and r.endDate)) and
            r.reservationStatus = ?4""")
    List<Reservation> findAllByAccommodationInPeriod(UUID accommodationId, LocalDate reservationStart, LocalDate reservationEnd, ReservationStatus reservationStatus);

    @Transactional
    @Modifying
    @Query("""
            update Reservation r set r.reservationStatus = ?1
            where r.accommodationId = ?3 and r.reservationStatus = ?2 and (r.startDate between ?4 and ?5 or r.endDate between ?4 and ?5)""")
    void updateReservationStatusByDate(ReservationStatus newStatus, ReservationStatus currentStatus, UUID id, LocalDate startDate, LocalDate endDate);

    Optional<Reservation> findByIdAndHostId(UUID id, UUID hostId);

    Optional<Reservation> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            select r from Reservation r
            where
            (?1 is null or r.userId = ?1) and
            (?2 is null or r.hostId = ?2) and
            (?3 is null or r.reservationStatus in ?3) and
            (?4 is null or r.accommodationId = ?4)"""
    )
    List<Reservation> filterAll(UUID userId, UUID hostId, List<ReservationStatus> statuses, UUID accommodationId);

    List<Reservation> findByHostIdAndReservationStatusIn(UUID hostId, List<ReservationStatus> statuses);

    @Query("""
        select r.accommodationId from Reservation r 
        where (?1 between r.startDate and r.endDate) or (?2 between r.startDate and r.endDate)
        and r.reservationStatus in ?3
    """)
    List<UUID> findAllUnavailable(LocalDate startDate, LocalDate endDate, List<ReservationStatus> statuses);
}
