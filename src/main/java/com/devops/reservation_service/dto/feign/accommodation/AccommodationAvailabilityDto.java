package com.devops.reservation_service.dto.feign.accommodation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationAvailabilityDto {
    private Long id;
    private List<RangePeriod> rangePeriods;
    private List<PatternPeriod> patternPeriods;
    private PriceDto price;
}
