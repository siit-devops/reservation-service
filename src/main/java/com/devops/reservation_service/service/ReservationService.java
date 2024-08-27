package com.devops.reservation_service.service;

import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.exception.BadRequestException;
import com.devops.reservation_service.exception.NotFoundException;
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
        // todo: notify all guests reservation_denied
    }

    private void validateReservationRequest(ReservationDto reservationDto) {
        if (reservationDto.getReservationStart().isAfter(reservationDto.getReservationEnd()))
            throw new BadRequestException("Start date cannot be after end date");

        if (reservationDto.getReservationStart().isBefore(LocalDate.now()))
            throw new BadRequestException("Start date cannot be in the past");
    }

    public void cancelReservation(String guestId, UUID reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUserId(
                reservationId,
                UUID.fromString(guestId)
        ).orElseThrow(() -> new NotFoundException("Reservation not found"));

        var acceptableStatuses = List.of(ReservationStatus.ACCEPTED, ReservationStatus.PENDING);
        if (!acceptableStatuses.contains(reservation.getReservationStatus())) {
            throw new BadRequestException("Reservation is not in ACCEPTED or PENDING status");
        }

        if (!LocalDate.now().isBefore(reservation.getStartDate())) {
            throw new BadRequestException("Minimum one day before start reservation can be cancelled");
        }

        if (reservation.getReservationStatus() == ReservationStatus.PENDING) {
            reservation.setReservationStatus(ReservationStatus.WITHDRAWN);
        }
        else {
            reservation.setReservationStatus(ReservationStatus.CANCELED);
            // todo: send notification reservation_canceled to host
        }
      
        reservationRepository.save(reservation);
    }
  
    public void respondToReservationRequest(String hostId, UUID reservationId, boolean accepted) {
        var reservation = reservationRepository.findByIdAndHostId(
                reservationId,
                UUID.fromString(hostId)
        ).orElseThrow(() -> new NotFoundException("Reservation not found"));

        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("The reservation is not in status PENDING");
        }

        if (accepted) {
            approveReservation(reservation);
            // todo: notify guest reservation_approved
        }
        else {
            reservation.setReservationStatus(ReservationStatus.DENIED);
            // todo: notify guest reservation_denied
        }
      
        reservationRepository.save(reservation);
    }
}
