package com.mattleibold.bulktracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;


/**
 * Created by Matt on 3/28/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BulkTrackerDB.db";

    public static class WeightEntry implements BaseColumns {
        public static final String WEIGHT_TABLE_NAME = "WeightHistory";
        public static final String ID_COLUMN_NAME = "id";
        public static final String POUNDS_COLUMN_NAME = "Pounds";
        public static final String DATE_COLUMN_NAME = "Date";
        public static final String TIME_COLUMN_NAME = "Time";
        public static final String COMMENT_COLUMN_NAME = "Comment";
        public static final String CREATE_TABLE_SQL =
                "CREATE TABLE " + WEIGHT_TABLE_NAME + "( " + ID_COLUMN_NAME +
                        " integer primary key, " + POUNDS_COLUMN_NAME + " real, " +
                        DATE_COLUMN_NAME + " text, " + TIME_COLUMN_NAME + " integer, " +
                        COMMENT_COLUMN_NAME + " text)";

        public static final String DROP_TABLE_SQL =
                "DROP TABLE IF EXISTS " + WEIGHT_TABLE_NAME;

        public static final String GET_ALL_SQL = "SELECT * FROM " + WEIGHT_TABLE_NAME +
                " ORDER BY DATE ASC, TIME ASC";

        public double weight;
        public String date;
        public int time; // time of day in seconds (between 0 and 60 * 60 * 24)
        public String comment;

        public WeightEntry(double w_in, String d_in, int t_in, String c_in) {
            weight = w_in;
            date = d_in;
            time = t_in;
            comment = c_in;
        }

        public String makeTimeString() {
            int hours = time / 3600;
            int minutes = (time - (hours * 3600)) / 60;
            String minuteStr = String.valueOf(minutes);
            if (minuteStr.length() < 2) minuteStr = "0" + minuteStr;
            String meridiem = "am";
            if (hours > 12) {
                hours -= 12;
                meridiem = "pm";
            } else if (hours == 12) {
                meridiem = "pm";
            }

            return String.valueOf(hours) + ":" + minuteStr + " " + meridiem;
        }

    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WeightEntry.CREATE_TABLE_SQL);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(WeightEntry.DROP_TABLE_SQL);
        onCreate(db);
    }

    public boolean insertWeight(double weight, String date, int time, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(WeightEntry.POUNDS_COLUMN_NAME, weight);
        cv.put(WeightEntry.DATE_COLUMN_NAME, date);
        cv.put(WeightEntry.TIME_COLUMN_NAME, time);
        cv.put(WeightEntry.COMMENT_COLUMN_NAME, comment);
        long row_id = db.insert(WeightEntry.WEIGHT_TABLE_NAME, null, cv);
        return row_id != -1;
    }

    public ArrayList<WeightEntry> getAllWeights() {
        ArrayList<WeightEntry> weights = new ArrayList<WeightEntry>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(WeightEntry.GET_ALL_SQL, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            double pounds = res.getDouble(res.getColumnIndex(WeightEntry.POUNDS_COLUMN_NAME));
            String date = res.getString(res.getColumnIndex(WeightEntry.DATE_COLUMN_NAME));
            int time = res.getInt(res.getColumnIndex(WeightEntry.TIME_COLUMN_NAME));
            String comment = res.getString(res.getColumnIndex(WeightEntry.COMMENT_COLUMN_NAME));
            WeightEntry entry = new WeightEntry(pounds, date, time, comment);
            weights.add(entry);
            res.moveToNext();
        }
        return weights;

    }


}