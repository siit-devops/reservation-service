package com.devops.reservation_service.service.feignClients;

import com.devops.reservation_service.dto.ReservationDto;
import com.devops.reservation_service.dto.feign.accommodation.AccommodationReservationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "accommodation-service", url = "${accommodation-service.ribbon.listOfServers}")
public interface AccommodationClient {

    @PostMapping("/api/internal/accommodations/make-reservation")
    AccommodationReservationDto makeReservationForAccommodation(@RequestBody ReservationDto reservationDto);
}
