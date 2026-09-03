package org.tss.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class HolidayService {
    public static final Set<String> SUPPORTED_STATES = Set.of("BW","BY","BE","BB","HB","HH","HE","MV","NI","NW","RP","SL","SN","ST","SH","TH");
    @Value("${tss.holidays.state:RP}")
    private String state = "RP";

    public boolean isWorkingDay(LocalDate date, int workingDaysPerWeek) {
        return isWorkingDay(date, workingDaysPerWeek, state);
    }

    public boolean isWorkingDay(LocalDate date, int workingDaysPerWeek, String federalState) {
        if (date.getDayOfWeek().getValue() > workingDaysPerWeek) return false;
        return !holidays(date.getYear(), federalState).contains(date);
    }

    public Set<LocalDate> holidays(int year, String federalState) {
        String code = federalState == null ? state : federalState.toUpperCase();
        if (!SUPPORTED_STATES.contains(code)) throw new IllegalArgumentException("Unsupported German federal state: " + code);
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
        if (Set.of("RP", "BW", "BY", "HE", "NW", "SL").contains(code)) {
            days.add(easter.plusDays(60));
        }
        if (Set.of("BW", "BY", "ST").contains(code)) days.add(LocalDate.of(year, 1, 6));
        if (Set.of("BW", "BY", "NW", "RP", "SL").contains(code)) days.add(LocalDate.of(year, 11, 1));
        if (Set.of("BB", "HB", "HH", "MV", "NI", "SH", "SN", "ST", "TH").contains(code)) days.add(LocalDate.of(year, 10, 31));
        if (Set.of("BE", "MV").contains(code)) days.add(LocalDate.of(year, 3, 8));
        if ("SL".equals(code)) days.add(LocalDate.of(year, 8, 15));
        if ("TH".equals(code)) days.add(LocalDate.of(year, 9, 20));
        if ("BB".equals(code)) { days.add(easter); days.add(easter.plusDays(49)); }
        if ("SN".equals(code)) days.add(repentanceDay(year));
        return days;
    }

    private LocalDate repentanceDay(int year) {
        LocalDate date = LocalDate.of(year, 11, 23);
        while (date.getDayOfWeek() != DayOfWeek.WEDNESDAY) date = date.minusDays(1);
        return date;
    }

    private LocalDate easterSunday(int year) {
        int a=year%19,b=year/100,c=year%100,d=b/4,e=b%4,f=(b+8)/25,g=(b-f+1)/3;
        int h=(19*a+b-d-g+15)%30,i=c/4,k=c%4,l=(32+2*e+2*i-h-k)%7,m=(a+11*h+22*l)/451;
        int month=(h+l-7*m+114)/31, day=(h+l-7*m+114)%31+1;
        return LocalDate.of(year, month, day);
    }
}
