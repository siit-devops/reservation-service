package com.devops.reservation_service.controller.internal;

import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/internal/reservations")
@CrossOrigin(origins = "http://localhost:4200/")
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

}
