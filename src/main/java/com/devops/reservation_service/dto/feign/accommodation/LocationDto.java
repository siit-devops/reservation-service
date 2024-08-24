package com.devops.reservation_service.dto.feign.accommodation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDto {

    private UUID id;
    private String name;
    private String fullAddress;
    private double lon;
    private double lat;
}
