package com.devops.reservation_service.service.feignClients;

import com.devops.reservation_service.dto.feign.accommodation.AccommodationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "accommodation-service", url = "${accommodation-service.ribbon.listOfServers}")
public interface AccommodationClient {

    @GetMapping("/api/internal/accommodations/{accommodationId}")
    AccommodationDto getAccommodationById(@PathVariable String accommodationId);

}
