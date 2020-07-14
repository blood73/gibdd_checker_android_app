package ru.bloodsoft.gibddchecker.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tenure {

    private static final Integer HOURS_IN_DAY = 24;
    private static final Integer MINUTES_IN_HOUR = 60;
    private static final Integer SECONDS_IN_MINUTE = 60;
    private static final Integer DAYS_IN_YEAR = 365;
    private static final Double DAYS_IN_MONTH = 30.416666667;

    public static String getTenureByDates(String dateFrom, String dateTo) {
        Long timestampFrom = getTimestampFromDate(dateFrom);
        Long timestampTo = getTimestampFromDate(dateTo);

        Long timestampDiff = timestampTo - timestampFrom;

        TenurePeriod period = getTenurePeriod(timestampDiff);
        String resultString = "";

        Integer years = period.getYears();
        Integer months = period.getMonths();
        Integer days = period.getDays();

        if (years > 0) {
            resultString = years + getYearEnding(years) + " ";
        }

        if (months > 0) {
            resultString += months + getMonthEnding(period.getMonths()) + " ";
        }

        if (days > 0) {

            if (months > 0) {
                resultString += "и ";
            }

            resultString += days + getDaysEnding(period.getDays());
        }

        return resultString;
    }

    private static Long getTimestampFromDate(String rawDate) {

        String inputFormat = "yyyy-MM-dd'T'HH:mm:ss";
        Long timestampResult;
        Date parsed;

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(rawDate);
            long output = parsed.getTime() / 1000L;
            String str = Long.toString(output);
            timestampResult = Long.parseLong(str);
        } catch (Exception e) {
            timestampResult = System.currentTimeMillis() / 1000;
        }

        return timestampResult;
    }

    private static TenurePeriod getTenurePeriod(Long timestampDiff) {
        TenurePeriod newPeriod = new TenurePeriod();
        Double days = (double) timestampDiff / (HOURS_IN_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE);
        Double years = days / DAYS_IN_YEAR;
        Integer yearsInt = (int) Math.floor(years);

        Double daysModuloFromYear = days % DAYS_IN_YEAR;
        Double months = daysModuloFromYear / DAYS_IN_MONTH;
        Integer monthsInt = (int) Math.floor(months);

        Double daysModuloFromMonth = (months * 30) % DAYS_IN_MONTH;
        Integer daysInt = (int) Math.floor(daysModuloFromMonth);

        if (daysInt == 31) {
            monthsInt += 1;
            daysInt = 0;
        }

        if (monthsInt == 12) {
            yearsInt += 1;
            monthsInt = 0;
        }

        if (yearsInt == 0 && monthsInt == 0 && daysInt == 0) {
            daysInt = days.intValue();
        }

        newPeriod.setYears(yearsInt);
        newPeriod.setMonths(monthsInt);
        newPeriod.setDays(daysInt);

        return newPeriod;
    }

    private static String getYearEnding (Integer years) {
        String result;

        if (years == 1) {
            result = " год";
        } else if (years > 1 && years <= 4) {
            result = " года";
        } else {
            result = " лет";
        }

        return result;
    }

    private static String getMonthEnding (Integer monthes) {
        String result;

        if (monthes == 1) {
            result = " месяц";
        } else if (monthes > 1 && monthes <= 4) {
            result = " месяца";
        } else {
            result = " месяцев";
        }

        return result;
    }

    private static String getDaysEnding (Integer days) {
        String result;

        if (days == 1) {
            result = " день";
        } else if (days > 1 && days <= 4) {
            result = " дня";
        } else {
            result = " дней";
        }

        return result;
    }
}