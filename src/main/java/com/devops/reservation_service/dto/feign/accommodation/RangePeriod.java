package com.devops.reservation_service.dto.feign.accommodation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RangePeriod extends PeriodDto {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;

    public boolean ruleAppliesToDay(LocalDate day) {
        return !day.isBefore(startDate) && !day.isAfter(endDate);
    }
}
