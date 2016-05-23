package com.mattleibold.bulktracker;

import android.provider.BaseColumns;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Matt on 5/23/2016.
 */
public class ProgressPicture implements BaseColumns {
    public static final String TABLE_NAME = "ProgressPicture";
    public static final String ID_COLUMN_NAME = "id";
    public static final String POUNDS_COLUMN_NAME = "Pounds";
    public static final String DATE_COLUMN_NAME = "Date";
    public static final String TIME_COLUMN_NAME = "Time";
    public static final String PATH_COLUMN_NAME = "Filepath";
    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + "( " + ID_COLUMN_NAME +
                    " integer primary key, " + POUNDS_COLUMN_NAME + " real, " +
                    DATE_COLUMN_NAME + " text, " + TIME_COLUMN_NAME + " integer, " +
                    PATH_COLUMN_NAME + " text)";

    public static final String DROP_TABLE_SQL =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String GET_ALL_SQL = "SELECT * FROM " + TABLE_NAME +
            " ORDER BY DATE ASC, TIME ASC";

    public static final String GET_MOST_RECENT_SQL = "SELECT * FROM " + TABLE_NAME +
            " ORDER BY DATE DESC, TIME DESC LIMIT 1";

    public double weight;
    public String date;
    public int time; // time of day in seconds (between 0 and 60 * 60 * 24)
    public String filepath;
    public int id;

    public ProgressPicture(double w_in, String d_in, int t_in, String fp_in) {
        weight = w_in; date = d_in; time = t_in; filepath = fp_in;
    }

    public ProgressPicture(double w_in, String d_in, int t_in, String fp_in, int id_in) {
        weight = w_in; date = d_in; time = t_in; filepath = fp_in; id = id_in;
    }

    public String makeTimeString() {
        return Utilities.secondsToTimeString(time);
    }

    public int makeTimestamp() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            Date parsedDate = formatter.parse(date);
            int timestamp = (int) (parsedDate.getTime() / 1000);
            return timestamp + time;
        } catch (ParseException e) {
            Log.w("BTLOG", "Parse exception for date string " + date);
            return -1;
        }
    }

    public Date makeDate() {
        return new Date((long)makeTimestamp() * 1000);
    }
}
