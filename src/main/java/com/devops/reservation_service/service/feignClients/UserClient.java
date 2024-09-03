package com.devops.reservation_service.service.feignClients;

import com.devops.reservation_service.dto.feign.user.UsersFromReservationDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;


@FeignClient(name = "user-service", url = "${user-service.ribbon.listOfServers}")
public interface UserClient {

    @GetMapping("/api/internal/users/host-and-guest-details")
    UsersFromReservationDetails getUsersForReservationDetails(@RequestParam UUID guestId, @RequestParam UUID hostId);
}
