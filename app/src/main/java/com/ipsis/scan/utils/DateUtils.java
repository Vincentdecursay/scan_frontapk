package com.ipsis.scan.utils;

import java.util.Calendar;

/**
 * Created by pobouteau on 9/29/16.
 */

public class DateUtils {

    /**
     * Return true if dates are the same day
     * @param date1 date 1
     * @param date2 date 2
     * @return true if dates are the same day
     */
    public static boolean isSameDay(Calendar date1, Calendar date2) {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR);
    }

    public static String formatDate(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return hour + ":" + (minute < 10 ? "0" + minute : minute);
    }

    public static String formatDate(int hour, int minute) {
        return hour + ":" + (minute < 10 ? "0" + minute : minute);
    }

    public static int compareDate(String date) {
        String[] tokens = date.split(":");

        if (tokens.length == 2) {
            return Integer.parseInt(tokens[0]) * 60 + Integer.parseInt(tokens[1]);
        } else {
            return -1;
        }
    }

    public static String hourToUTC(String hour) {
        return Calendar.YEAR + "/" + Calendar.MONTH + "/" + Calendar.DAY_OF_MONTH + " " + hour;
    }
}
