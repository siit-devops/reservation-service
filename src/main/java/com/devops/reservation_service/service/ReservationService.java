package com.devops.reservation_service.service;

import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.exception.BadRequestException;
import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.model.enumerations.ReservationPeriod;
import com.devops.reservation_service.model.enumerations.ReservationStatus;
import com.devops.reservation_service.repository.ReservationRepository;
import com.devops.reservation_service.service.feignClients.AccommodationClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final AccommodationClient accommodationClient;

    public ReservationService(ReservationRepository reservationRepository, AccommodationClient accommodationClient) {
        this.reservationRepository = reservationRepository;
        this.accommodationClient = accommodationClient;
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

    public Reservation createReservationRequest(String guestId, ReservationDto reservationDto) {
        validateReservationRequest(reservationDto);
        var reserved = checkIfAccommodationHasReservationsForPeriod(reservationDto.getAccommodationId(), reservationDto.getReservationStart(), reservationDto.getReservationEnd());
        if (reserved)
            throw new BadRequestException("The accommodation has already been reserved");

        var accommodationReservation = accommodationClient.makeReservationForAccommodation(reservationDto);
        if (accommodationReservation == null) {
            throw new BadRequestException("The accommodation is not available for this reservation");
        }

        var reservation = Reservation.builder()
                .reservationStatus(ReservationStatus.PENDING)
                .hostId(reservationDto.getHostId())
                .userId(UUID.fromString(guestId))
                .accommodationId(reservationDto.getAccommodationId())
                .guestsNumber(reservationDto.getGuestNumber())
                .totalPrice(accommodationReservation.getTotalPrice())
                .startDate(reservationDto.getReservationStart())
                .endDate(reservationDto.getReservationEnd())
                .build();

        if (accommodationReservation.isAutoApproveReservation()) {
            approveReservation(reservation);
        }
        reservationRepository.save(reservation);

        // todo: send notification to host
        return reservation;
    }

    public boolean checkIfAccommodationHasReservationsForPeriod(UUID accommodationId, LocalDate startDate, LocalDate endDate) {
        var reservations = reservationRepository.findAllByAccommodationInPeriod(
                accommodationId,
                startDate,
                endDate,
                ReservationStatus.ACCEPTED
        );
        return !reservations.isEmpty();
    }

    private void approveReservation(Reservation reservation) {
        reservation.setReservationStatus(ReservationStatus.ACCEPTED);
        reservationRepository.updateReservationStatusByDate(
                ReservationStatus.DENIED,
                ReservationStatus.PENDING,
                reservation.getAccommodationId(),
                reservation.getStartDate(),
                reservation.getEndDate()
        );
    }

    private void validateReservationRequest(ReservationDto reservationDto) {
        if (reservationDto.getReservationStart().isAfter(reservationDto.getReservationEnd()))
            throw new BadRequestException("Start date cannot be after end date");

        if (reservationDto.getReservationStart().isBefore(LocalDate.now()))
            throw new BadRequestException("Start date cannot be in the past");
    }
}
