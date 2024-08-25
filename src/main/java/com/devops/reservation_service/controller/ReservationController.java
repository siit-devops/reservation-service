package com.devops.reservation_service.controller;

import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.service.ReservationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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
}
