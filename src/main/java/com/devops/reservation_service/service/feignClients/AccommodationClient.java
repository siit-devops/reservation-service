package com.devops.reservation_service.service.feignClients;

import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.dto.feign.accommodation.AccommodationDetails;
import com.devops.reservation_service.dto.feign.accommodation.AccommodationReservationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;


@FeignClient(name = "accommodation-service", url = "${accommodation-service.ribbon.listOfServers}")
public interface AccommodationClient {

    @PostMapping("/api/internal/accommodations/make-reservation")
    AccommodationReservationDto makeReservationForAccommodation(@RequestBody ReservationDto reservationDto);

    @GetMapping("/api/internal/accommodations/{id}")
    AccommodationDetails getAccommodationDetails(@PathVariable UUID id);
}
