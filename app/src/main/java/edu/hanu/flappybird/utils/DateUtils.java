package edu.hanu.flappybird.utils;

import android.util.Log;

import java.util.Calendar;

public class DateUtils {
    static Calendar calendar, calendar2;
    public static long getTimeToGo() {
        calendar = Calendar.getInstance();
        calendar2 = Calendar.getInstance();
        long timeToSchedule;
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = 7;
        int currentMinute = 0;
        calendar.set(currentYear, currentMonth, currentDayOfMonth, currentHour, currentMinute);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(currentYear, currentMonth, currentDayOfMonth);

            int nextYear = (currentMonth == Calendar.DECEMBER &&
                    currentDayOfMonth == calendar.getMaximum(Calendar.DAY_OF_MONTH)) ?
                    currentYear + 1 : currentYear;
            int nextMonth = currentDayOfMonth == calendar.getMaximum(Calendar.DAY_OF_MONTH) ?
                    currentMonth == Calendar.DECEMBER ? Calendar.JANUARY : currentMonth + 1 : currentMonth;
            int nextDay = currentDayOfMonth == calendar.getMaximum(Calendar.DAY_OF_MONTH) ?
                    calendar.getMinimum(Calendar.DAY_OF_MONTH) : currentDayOfMonth + 1;

            calendar2.set(Calendar.HOUR_OF_DAY, 7);
            calendar2.set(Calendar.MINUTE, 0);
            calendar2.set(nextYear, nextMonth, nextDay);

            timeToSchedule = calendar2.getTimeInMillis() - calendar.getTimeInMillis();
        } else {
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(currentYear, currentMonth, currentDayOfMonth);
            calendar2.set(Calendar.HOUR_OF_DAY, 7);
            calendar2.set(Calendar.MINUTE, 0);
            calendar2.set(currentYear, currentMonth, currentDayOfMonth);
            timeToSchedule = calendar2.getTimeInMillis() - calendar.getTimeInMillis();
        }
        return timeToSchedule;
    }
}
