package com.devops.reservation_service.dto.feign.accommodation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceRuleDto {
    private Long id;
    private double specialPrice;
    private RangePeriod rangePeriod;
    private PatternPeriod patternPeriod;

}
