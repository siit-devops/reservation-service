package com.devops.reservation_service.controller;

import com.devops.reservation_service.dto.GetReservationDto;
import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.model.Reservation;
import com.devops.reservation_service.model.enumerations.ReservationStatus;
import com.devops.reservation_service.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Reservation createReservation(Principal principal, @Valid @RequestBody ReservationDto reservationDto) {
        return reservationService.createReservationRequest(principal.getName(), reservationDto);
    }

    @DeleteMapping("/guests/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(Principal principal, @PathVariable UUID reservationId) {
        reservationService.cancelReservation(principal.getName(), reservationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/hosts/respond/{reservationId}")
    public ResponseEntity<?> respondToReservationRequest(
            Principal principal,
            @PathVariable UUID reservationId,
            @RequestParam boolean accepted
    ) {
        reservationService.respondToReservationRequest(principal.getName(), reservationId, accepted);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(reservationService.findById(id), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<GetReservationDto>> getAllByQueryParams(
            @RequestParam(required = false) Optional<UUID> userId,
            @RequestParam(required = false) Optional<UUID> hostId,
            @RequestParam(required = false) Optional<List<ReservationStatus>> statuses,
            @RequestParam(required = false) Optional<UUID> accommodationId
    ) {
        return new ResponseEntity<>(reservationService.getReservations(userId, hostId, statuses, accommodationId), HttpStatus.OK);
    }

    @GetMapping("/hosts")
    public ResponseEntity<List<GetReservationDto>> getAllByHostId(Principal principal, @RequestParam List<ReservationStatus> reservationStatuses) {
        return new ResponseEntity<>(reservationService.getAllByHostId(principal.getName(), reservationStatuses), HttpStatus.OK);
    }
}
