package com.devops.reservation_service.dto.feign.accommodation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatternPeriod extends PeriodDto{
    private Long id;
    private Set<DayOfWeek> daysOfWeek;

    public Set<LocalDate> getPeriodDates(LocalDate start, LocalDate end) {
        Set<LocalDate> resultDates = new HashSet<>();
        if (daysOfWeek.isEmpty() || start.isAfter(end))
            return resultDates;

        while (!start.atStartOfDay().equals(end.atStartOfDay())) {
            if (daysOfWeek.contains(start.getDayOfWeek())) {
                resultDates.add(LocalDate.from(start));
            }
            start = start.plusDays(1);
        }
        return resultDates;
    }

    public boolean ruleAppliesToDay(LocalDate day) {
        return daysOfWeek.contains(day.getDayOfWeek());
    }
}
