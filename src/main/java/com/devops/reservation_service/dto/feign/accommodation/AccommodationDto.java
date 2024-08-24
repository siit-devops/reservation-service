package com.devops.reservation_service.dto.feign.accommodation;

import com.devops.reservation_service.exception.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccommodationDto {
    private UUID id;
    private UUID hostId;
    private String name;
    private String description;
    private int minGuestNumber = 0;
    private int maxGuestNumber;
    private boolean autoApproveReservation;
    private Set<String> tags;
    private Set<String> images;
    private LocationDto location;
    private AccommodationAvailabilityDto availability;

    public boolean isAvailable(LocalDate reservationStart, LocalDate reservationEnd) {
        if (reservationStart == null || reservationEnd == null) {
            throw new BadRequestException("Reservation start and end dates are invalid");
        }
        Set<LocalDate> rangeDatesOfInterest = new HashSet<>();

        if (rangePeriodExist(reservationStart, reservationEnd, rangeDatesOfInterest))
            return true;

        var reservationDays = dateRangeToDays(reservationStart, reservationEnd);
        Set<LocalDate> patternDays = availability.getPatternPeriods()
                .stream()
                .map(patternPeriod -> patternPeriod.getPeriodDates(reservationStart, reservationEnd))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        if (patternDays.containsAll(reservationDays))
            return true;

        if (!rangeDatesOfInterest.isEmpty()) {
            rangeDatesOfInterest.addAll(patternDays);
            return rangeDatesOfInterest.containsAll(reservationDays);
        }
        return false;
    }

    private boolean rangePeriodExist(LocalDate reservationStart, LocalDate reservationEnd, Set<LocalDate> rangeDatesOfInterest) {
        for (RangePeriod rangePeriod: availability.getRangePeriods()) {
            boolean startMatches = !reservationStart.isBefore(rangePeriod.getStartDate());
            boolean endMatches = !reservationEnd.isAfter(rangePeriod.getEndDate());
            if (startMatches && endMatches) {
                return true;
            }

            if (startMatches) {
                rangeDatesOfInterest.addAll(dateRangeToDays(reservationStart, rangePeriod.getEndDate()));
                break;
            }
            if (endMatches) {
                rangeDatesOfInterest.addAll(dateRangeToDays(rangePeriod.getStartDate(), reservationEnd));
                break;
            }
        }
        return false;
    }

    private Collection<LocalDate> dateRangeToDays(LocalDate start, LocalDate end) {
        Set<LocalDate> days = new HashSet<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            days.add(current);
            current = current.plusDays(1);
        }
        return days;
    }

    public double getTotalPriceForReservation(LocalDate reservationStart, LocalDate reservationEnd) {
        var days = dateRangeToDays(reservationStart, reservationEnd);

        return days
                .stream()
                .map(day -> availability.getPrice().calculatePriceForDay(day))
                .reduce(0.0, Double::sum);
    }
}
