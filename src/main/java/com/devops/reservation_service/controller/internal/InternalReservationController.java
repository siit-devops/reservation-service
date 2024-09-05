package com.devops.reservation_service.controller.internal;

import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/internal/reservations")
public class InternalReservationController {

    private final ReservationService reservationService;

    public InternalReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{userId}")
    List<Reservation> getReservationsForUser(@PathVariable String userId, @RequestParam String reservationPeriod){
        return reservationService.getReservationsForUser(UUID.fromString(userId), reservationPeriod);
    }

    @GetMapping("/{userId}/count")
    int countUsersReservations(@PathVariable String userId, @RequestParam String reservationPeriod){
        return reservationService.countUsersReservations(UUID.fromString(userId), reservationPeriod);
    }

    @GetMapping("/has-previous/{userId}")
    boolean checkIfUserHasReservation(@PathVariable String userId, @RequestParam String hostId){
        return reservationService.checkIfUserHasReservations(UUID.fromString(userId), UUID.fromString(hostId));
    }

    @GetMapping("/stayed/{userId}")
    boolean userStayedAtAccomodation(@PathVariable String userId, @RequestParam String accomodationId){
        return reservationService.hasUserStayedAtAcccomodation(UUID.fromString(userId), UUID.fromString(accomodationId));
    }

    @GetMapping("/unavailable-accomodations")
    List<UUID> getUnavailableAccomodations(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return reservationService.getUnavailableAccomodations(startDate, endDate);
    }

    @DeleteMapping("/deleteByAccommodationId/{id}")
    boolean deleteReservationsByAccommodationId(@PathVariable UUID id){
        return reservationService.deleteByAccommodationId(id);
    }
}
