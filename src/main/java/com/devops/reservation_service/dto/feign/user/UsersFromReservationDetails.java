package com.devops.reservation_service.dto.feign.user;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UsersFromReservationDetails {
    private String hostName;
    private String guestName;
}
