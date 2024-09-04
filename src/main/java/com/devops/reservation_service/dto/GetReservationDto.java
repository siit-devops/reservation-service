package com.devops.reservation_service.dto;

import com.devops.reservation_service.model.enumerations.ReservationStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetReservationDto {
    private UUID id;
    private String accommodationName;
    private String hostName;
    private String guestName;
    private int guestNumber;
    private ReservationStatus reservationStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;
    private double pricePerGuest;
    private Set<String> images;
    private UUID accommodationId;
    private UUID hostId;
}
