package com.devops.reservation_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;


@NoArgsConstructor
@Data
@AllArgsConstructor
public class ReservationDto {
    private UUID accommodationId;
    private UUID hostId;
    private Integer guestNumber;
    private Double totalPrice;
    private Double priceByGuest;
    private LocalDate reservationStart;
    private LocalDate reservationEnd;
}
