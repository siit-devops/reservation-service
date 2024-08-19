package com.devops.reservation_service.service;

import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.model.enumerations.ReservationPeriod;
import com.devops.reservation_service.model.enumerations.ReservationStatus;
import com.devops.reservation_service.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }


    public List<Reservation> getReservationsForUser(UUID userId, String reservationPeriod) {
        List<Reservation> reservations;
        if (ReservationPeriod.valueOf(reservationPeriod) == ReservationPeriod.FUTURE) {
            reservations = reservationRepository.findAllByUserIdOrHostIdAAndReservationStatus(
                    userId,
                    userId,
                    List.of(ReservationStatus.IN_PROGRESS, ReservationStatus.ACCEPTED)
            );
        } else {
            reservations = reservationRepository.findAllByUserIdOrHostIdAAndReservationStatus(
                    userId,
                    userId,
                    List.of(ReservationStatus.DONE)
            );
        }

        return reservations;
    }

    public int countUsersReservations(UUID userId, String reservationPeriod) {
        return getReservationsForUser(userId, reservationPeriod).size();
    }
}
