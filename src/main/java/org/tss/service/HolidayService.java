package org.tss.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class HolidayService {
    @Value("${tss.holidays.state:RP}")
    private String state = "RP";

    public boolean isWorkingDay(LocalDate date, int workingDaysPerWeek) {
        if (date.getDayOfWeek().getValue() > workingDaysPerWeek) return false;
        return !holidays(date.getYear(), state).contains(date);
    }

    public Set<LocalDate> holidays(int year, String federalState) {
        Set<LocalDate> days = new HashSet<>();
        days.add(LocalDate.of(year, 1, 1));
        days.add(LocalDate.of(year, 5, 1));
        days.add(LocalDate.of(year, 10, 3));
        days.add(LocalDate.of(year, 12, 25));
        days.add(LocalDate.of(year, 12, 26));
        LocalDate easter = easterSunday(year);
        days.add(easter.minusDays(2));
        days.add(easter.plusDays(1));
        days.add(easter.plusDays(39));
        days.add(easter.plusDays(50));
        if (Set.of("RP", "BW", "BY", "HE", "NW", "SL").contains(federalState)) {
            days.add(easter.plusDays(60));
        }
        if (Set.of("BW", "BY", "ST").contains(federalState)) days.add(LocalDate.of(year, 1, 6));
        if (Set.of("BW", "BY", "NW", "RP", "SL").contains(federalState)) days.add(LocalDate.of(year, 11, 1));
        return days;
    }

    private LocalDate easterSunday(int year) {
        int a=year%19,b=year/100,c=year%100,d=b/4,e=b%4,f=(b+8)/25,g=(b-f+1)/3;
        int h=(19*a+b-d-g+15)%30,i=c/4,k=c%4,l=(32+2*e+2*i-h-k)%7,m=(a+11*h+22*l)/451;
        int month=(h+l-7*m+114)/31, day=(h+l-7*m+114)%31+1;
        return LocalDate.of(year, month, day);
    }
}
