package com.devops.reservation_service.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;


@NoArgsConstructor
@Data
@AllArgsConstructor
public class ReservationDto {
    @NotNull(message = "AccommodationID is required")
    private UUID accommodationId;
    @NotNull(message = "HostID is required")
    private UUID hostId;
    private Integer guestNumber = 1;
//    @NotNull
    private Double totalPrice;
    private Double priceByGuest;
    @NotNull(message = "Reservation Start Date is required!")
    private LocalDate reservationStart;
    @NotNull(message = "Reservation End Date is required")
    private LocalDate reservationEnd;
}
