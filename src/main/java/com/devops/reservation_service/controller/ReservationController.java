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


    @PutMapping("/hosts/respond/{reservationId}")
//  todo: @HasRole("HOST")
    public ResponseEntity<?> respondToReservationRequest(
            Principal principal,
            @PathVariable UUID reservationId,
            @RequestParam boolean accepted
    ) {
        reservationService.respondToReservationRequest(principal.getName(), reservationId, accepted);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
