package com.mattleibold.bulktracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Matt on 4/6/2016.
 */
public class Utilities {
    public static String getCurrentTimeString() {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        Date now = new Date();
        calendar.setTime(now);
        DateFormat df = new SimpleDateFormat("KK:mm:ss a");
        String time = df.format(calendar.getTime());
        return time;
    }

    public static int getSecondsSinceStartOfDay() {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        Date now = new Date();
        calendar.setTime(now);
        int time = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60;
        time += calendar.get(Calendar.MINUTE) * 60;
        time += calendar.get(Calendar.SECOND);
        return time;
    }

    public static String getCurrentDateString() {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        Date now = new Date();
        calendar.setTime(now);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String date = df.format(calendar.getTime());
        return date;
    }

    public static String secondsToTimeString(int secsFromStartOfDay) {
        int hours = secsFromStartOfDay / 3600;
        int minutes = (secsFromStartOfDay - (hours * 3600)) / 60;
        String minuteStr = String.valueOf(minutes);
        if (minuteStr.length() < 2) minuteStr = "0" + minuteStr;
        String meridiem = "am";
        if (hours > 12) {
            hours -= 12;
            meridiem = "pm";
        } else if (hours == 12) {
            meridiem = "pm";
        } else if (hours == 0) {
            hours = 12;
        }
        return String.valueOf(hours) + ":" + minuteStr + " " + meridiem;
    }

}
