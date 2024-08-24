package com.devops.reservation_service.dto.feign.accommodation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceDto {
    private Long id;
    private double basePrice;
    private boolean byPerson;
    Set<PriceRuleDto> priceRules;

    public double calculatePriceForDay(LocalDate day) {
        if (priceRules == null)
            return basePrice;

        for (PriceRuleDto rule : priceRules) {
            if (rule.getPatternPeriod() != null && rule.getPatternPeriod().ruleAppliesToDay(day))
                return rule.getSpecialPrice();
            if (rule.getRangePeriod() != null && rule.getRangePeriod().ruleAppliesToDay(day)) {
                return rule.getSpecialPrice();
            }
        }
        return basePrice;
    }
}
