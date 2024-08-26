package com.devops.reservation_service.controller;

import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Reservation createReservation(Principal principal, @RequestBody ReservationDto reservationDto) {
        return reservationService.createReservationRequest(principal.getName(), reservationDto);
    }

    @DeleteMapping("/guests/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(Principal principal, @PathVariable UUID reservationId) {
        reservationService.cancelReservation(principal.getName(), reservationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
