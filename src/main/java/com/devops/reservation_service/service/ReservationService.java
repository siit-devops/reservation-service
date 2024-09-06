package com.devops.reservation_service.service;

import com.devops.reservation_service.dto.GetReservationDto;
import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.dto.feign.accommodation.AccommodationDetails;
import com.devops.reservation_service.dto.feign.user.UsersFromReservationDetails;
import com.devops.reservation_service.exception.BadRequestException;
import com.devops.reservation_service.exception.NotFoundException;
import com.devops.reservation_service.kafka.KafkaProducer;
import com.devops.reservation_service.kafka.ReservationStatusUpdateMessage;
import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.model.enumerations.ReservationPeriod;
import com.devops.reservation_service.model.enumerations.ReservationStatus;
import com.devops.reservation_service.repository.ReservationRepository;
import com.devops.reservation_service.service.feignClients.AccommodationClient;
import com.devops.reservation_service.service.feignClients.UserClient;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final AccommodationClient accommodationClient;
    private final KafkaProducer publisher;
    private final UserClient userClient;

    public ReservationService(ReservationRepository reservationRepository, AccommodationClient accommodationClient, KafkaProducer publisher, UserClient userClient) {
        this.reservationRepository = reservationRepository;
        this.accommodationClient = accommodationClient;
        this.publisher = publisher;
        this.userClient = userClient;
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

        sendNotificationToUser(reservation.getUserId(), reservation.getHostId(), reservation.getId(), ReservationStatus.PENDING);
        return reservation;
    }

    public boolean checkIfAccommodationHasReservationsForPeriod(UUID accommodationId, LocalDate startDate, LocalDate endDate) {
        var reservations = reservationRepository.findAllByAccommodationInPeriod(
                accommodationId,
                startDate,
                endDate,
                List.of(ReservationStatus.ACCEPTED)
        );
        return !reservations.isEmpty();
    }

    private void approveReservation(Reservation reservation) {
        reservation.setReservationStatus(ReservationStatus.ACCEPTED);
        reservationRepository.save(reservation);

        List<Reservation> deniedReservations = reservationRepository.findAllByAccommodationInPeriod(
                reservation.getAccommodationId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                List.of(ReservationStatus.PENDING)
        );

        reservationRepository.updateReservationStatusByDate(
                ReservationStatus.DENIED,
                ReservationStatus.PENDING,
                reservation.getAccommodationId(),
                reservation.getStartDate(),
                reservation.getEndDate()
        );

        for (Reservation deniedReservation : deniedReservations) {
            sendNotificationToUser(reservation.getHostId(), deniedReservation.getUserId(), deniedReservation.getId(), ReservationStatus.DENIED);
        }
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
            sendNotificationToUser(reservation.getUserId(), reservation.getHostId(), reservation.getId(), ReservationStatus.WITHDRAWN);
        } else {
            reservation.setReservationStatus(ReservationStatus.CANCELED);
            sendNotificationToUser(reservation.getUserId(), reservation.getHostId(), reservation.getId(), ReservationStatus.CANCELED);
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
            sendNotificationToUser(reservation.getHostId(), reservation.getUserId(), reservation.getId(), ReservationStatus.ACCEPTED);
        } else {
            reservation.setReservationStatus(ReservationStatus.DENIED);
            sendNotificationToUser(reservation.getHostId(), reservation.getUserId(), reservation.getId(), ReservationStatus.DENIED);
        }

        reservationRepository.save(reservation);
    }

    public boolean checkIfUserHasReservations(UUID guestId, UUID hostId) {
        return !reservationRepository.findAllByUserIdAndHostIdAAndReservationStatus(guestId, hostId, List.of(ReservationStatus.DONE, ReservationStatus.ACCEPTED)).isEmpty();
    }

    public boolean hasUserStayedAtAcccomodation(UUID guestId, UUID accomodationId) {
        return !reservationRepository.findAllByUserIdAndAccomodationIdAAndReservationStatus(guestId, accomodationId, List.of(ReservationStatus.DONE, ReservationStatus.ACCEPTED)).isEmpty();
    }

    public Reservation findById(UUID id) {
        return reservationRepository.findById(id).orElseThrow(() -> new NotFoundException("Reservation not found"));
    }

    public List<Reservation> getAllReservations(Optional<UUID> userId, Optional<UUID> hostId, Optional<List<ReservationStatus>> statuses, Optional<UUID> accommodationId) {
        var userIdValue = userId.orElse(null);
        var statusValue = statuses.orElse(null);
        var accommodationIdValue = accommodationId.orElse(null);
        var hostIdValue = hostId.orElse(null);
        return reservationRepository.filterAll(userIdValue, hostIdValue, statusValue, accommodationIdValue);
    }

    public List<GetReservationDto> getAllByHostId(String hostIdStr, Optional<List<ReservationStatus>> statuses) {
        var hostId = UUID.fromString(hostIdStr);
        var statusValues = statuses.orElse(null);
        var reservations = reservationRepository.findByHostIdAndReservationStatusIn(hostId, statusValues);

        return makeReservationsDto(reservations);
    }

    private List<GetReservationDto> makeReservationsDto(List<Reservation> reservations) {
        List<GetReservationDto> reservationInfos = new ArrayList<>();

        for (Reservation reservation : reservations) {
            AccommodationDetails accommodationDetails = accommodationClient.getAccommodationDetails(reservation.getAccommodationId());
            UsersFromReservationDetails usersDetails = userClient.getUsersForReservationDetails(reservation.getUserId(), reservation.getHostId());

            reservationInfos.add(GetReservationDto.builder()
                    .id(reservation.getId())
                    .accommodationId(reservation.getAccommodationId())
                    .hostId(reservation.getHostId())
                    .hostName(usersDetails.getHostName())
                    .guestName(usersDetails.getGuestName())
                    .reservationStatus(reservation.getReservationStatus())
                    .images(accommodationDetails.getImages())
                    .accommodationName(accommodationDetails.getName())
                    .guestNumber(reservation.getGuestsNumber())
                    .startDate(reservation.getStartDate())
                    .endDate(reservation.getEndDate())
                    .totalPrice(reservation.getTotalPrice())
                    .pricePerGuest((reservation.getPriceByGuest() != null) ? reservation.getPriceByGuest() : 0.)
                    .build()
            );
        }

        return reservationInfos;
    }

    public List<UUID> getUnavailableAccomodations(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findAllUnavailable(startDate, endDate, List.of(ReservationStatus.IN_PROGRESS,
                ReservationStatus.ACCEPTED));
    }

    private void sendNotificationToUser(UUID senderId, UUID receiverId, UUID reservationId, ReservationStatus status) {
        publisher.send("reservation-status-update",
                new ReservationStatusUpdateMessage(senderId, receiverId, reservationId, status));
    }

    public List<GetReservationDto> getReservations(Optional<UUID> userId, Optional<UUID> hostId, Optional<List<ReservationStatus>> statuses, Optional<UUID> accommodationId) {
        var reservations = getAllReservations(userId, hostId, statuses, accommodationId);
        return makeReservationsDto(reservations);
    }

    @Transactional
    public boolean deleteByAccommodationId(UUID accommodationId) {
        var reservations = reservationRepository.findAllByAccommodationIdAndReservationStatusIn(accommodationId, List.of(ReservationStatus.ACCEPTED, ReservationStatus.IN_PROGRESS));
        if (reservations.isEmpty()) {
            reservationRepository.removeAllByAccommodationId(accommodationId);
            return true;
        }
        return false;
    }
}
