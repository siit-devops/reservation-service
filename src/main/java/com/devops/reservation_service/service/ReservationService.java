package com.devops.reservation_service.service;

import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.dto.feign.accommodation.AccommodationDto;
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
        var accommodation = checkIfAccommodationIsAvailable(reservationDto);
        double totalPrice = accommodation.getTotalPriceForReservation(reservationDto.getReservationStart(), reservationDto.getReservationEnd());

        var reservation = Reservation.builder()
                .reservationStatus(ReservationStatus.PENDING)
                .hostId(reservationDto.getHostId())
                .userId(UUID.fromString(guestId))
                .accommodationId(reservationDto.getAccommodationId())
                .guestsNumber(reservationDto.getGuestNumber())
                .totalPrice(totalPrice)
                .startDate(reservationDto.getReservationStart())
                .endDate(reservationDto.getReservationEnd())
                .build();

        if (accommodation.isAutoApproveReservation()){
            approveReservation(reservation);
        }
        reservationRepository.save(reservation);

        // todo: send notification to host
        return reservation;
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

    private AccommodationDto checkIfAccommodationIsAvailable(ReservationDto reservationDto) {
        var reservations = reservationRepository.findAllByAccommodationInPeriod(
                reservationDto.getAccommodationId(),
                reservationDto.getReservationStart(),
                reservationDto.getReservationEnd(),
                ReservationStatus.ACCEPTED
        );
        if (!reservations.isEmpty()) {
            throw new BadRequestException("Accommodation already has reservations for selected period");
        }

        var accommodation = accommodationClient.getAccommodationById(reservationDto.getAccommodationId().toString());
        if (!accommodation.isAvailable(reservationDto.getReservationStart(), reservationDto.getReservationEnd())) {
            throw new BadRequestException("Accommodation is not available for selected period");
        }

        if (reservationDto.getGuestNumber() < accommodation.getMinGuestNumber()
                || reservationDto.getGuestNumber() > accommodation.getMaxGuestNumber()) {
            throw new BadRequestException("Guest number is not acceptable");
        }

        return accommodation;
    }

    private void validateReservationRequest(ReservationDto reservationDto) {
        if (reservationDto.getReservationStart().isAfter(reservationDto.getReservationEnd()))
            throw new BadRequestException("Start date cannot be after end date");

        if (reservationDto.getReservationStart().isBefore(LocalDate.now()))
            throw new BadRequestException("Start date cannot be in the past");
    }
}
