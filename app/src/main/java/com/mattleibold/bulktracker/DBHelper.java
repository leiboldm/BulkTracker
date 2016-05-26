package com.mattleibold.bulktracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;


/**
 * Created by Matt on 3/28/2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "BulkTrackerDB.db";

    public static class WeightEntry implements BaseColumns {
        public static final String TABLE_NAME = "WeightHistory";
        public static final String ID_COLUMN_NAME = "id";
        public static final String POUNDS_COLUMN_NAME = "Pounds";
        public static final String DATE_COLUMN_NAME = "Date";
        public static final String TIME_COLUMN_NAME = "Time";
        public static final String COMMENT_COLUMN_NAME = "Comment";
        public static final String CREATE_TABLE_SQL =
                "CREATE TABLE " + TABLE_NAME + "( " + ID_COLUMN_NAME +
                        " integer primary key, " + POUNDS_COLUMN_NAME + " real, " +
                        DATE_COLUMN_NAME + " text, " + TIME_COLUMN_NAME + " integer, " +
                        COMMENT_COLUMN_NAME + " text)";

        public static final String DROP_TABLE_SQL =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String GET_ALL_SQL = "SELECT * FROM " + TABLE_NAME +
                " ORDER BY DATE ASC, TIME ASC";

        public static final String GET_MOST_RECENT_SQL = "SELECT * FROM " + TABLE_NAME +
                " ORDER BY DATE DESC, TIME DESC LIMIT 1";

        public double weight;
        public String date;
        public int time; // time of day in seconds (between 0 and 60 * 60 * 24)
        public String comment;
        public int id;

        public WeightEntry(double w_in, String d_in, int t_in, String c_in) {
            weight = w_in; date = d_in; time = t_in; comment = c_in;
        }

        public WeightEntry(double w_in, String d_in, int t_in, String c_in, int id_in) {
            weight = w_in; date = d_in; time = t_in; comment = c_in; id = id_in;
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

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WeightEntry.CREATE_TABLE_SQL);
        db.execSQL(ProgressPicture.CREATE_TABLE_SQL);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL(ProgressPicture.CREATE_TABLE_SQL);
        }
    }

    public boolean insertWeight(double weight, String date, int time, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(WeightEntry.POUNDS_COLUMN_NAME, weight);
        cv.put(WeightEntry.DATE_COLUMN_NAME, date);
        cv.put(WeightEntry.TIME_COLUMN_NAME, time);
        cv.put(WeightEntry.COMMENT_COLUMN_NAME, comment);
        long row_id = db.insert(WeightEntry.TABLE_NAME, null, cv);
        return row_id != -1;
    }

    public boolean deleteWeightEntry(int row_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(WeightEntry.TABLE_NAME,
                WeightEntry.ID_COLUMN_NAME + " = " + row_id, null) > 0;
    }

    // returns the number of weights the user has entered
    public int getWeightsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(WeightEntry.GET_MOST_RECENT_SQL, null);
        int count = res.getCount();
        res.close();
        return count;
    }

    // returns the most recent weight the user has entered
    public WeightEntry getMostRecentWeight() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(WeightEntry.GET_MOST_RECENT_SQL, null);
        res.moveToFirst();
        return makeWeight(res);
    }

    // returns a list of all the weights the user has entered
    public ArrayList<WeightEntry> getAllWeights() {
        ArrayList<WeightEntry> weights = new ArrayList<WeightEntry>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(WeightEntry.GET_ALL_SQL, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            weights.add(makeWeight(res));
            res.moveToNext();
        }
        return weights;
    }

    private WeightEntry makeWeight(Cursor res) {
        double pounds = res.getDouble(res.getColumnIndex(WeightEntry.POUNDS_COLUMN_NAME));
        String date = res.getString(res.getColumnIndex(WeightEntry.DATE_COLUMN_NAME));
        int time = res.getInt(res.getColumnIndex(WeightEntry.TIME_COLUMN_NAME));
        String comment = res.getString(res.getColumnIndex(WeightEntry.COMMENT_COLUMN_NAME));
        int id = res.getInt(res.getColumnIndex(WeightEntry.ID_COLUMN_NAME));
        return new WeightEntry(pounds, date, time, comment, id);
    }

    public boolean insertProgressPicture(double weight, String date, int time, String filepath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ProgressPicture.POUNDS_COLUMN_NAME, weight);
        cv.put(ProgressPicture.DATE_COLUMN_NAME, date);
        cv.put(ProgressPicture.TIME_COLUMN_NAME, time);
        cv.put(ProgressPicture.PATH_COLUMN_NAME, filepath);
        long row_id = db.insert(ProgressPicture.TABLE_NAME, null, cv);
        return row_id != -1;
    }

    public boolean deleteProgressPicture(int row_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(ProgressPicture.TABLE_NAME,
                ProgressPicture.ID_COLUMN_NAME + " = " + row_id, null) > 0;
    }

    public Vector<ProgressPicture> getAllProgressPictures() {
        Vector<ProgressPicture> pics = new Vector<ProgressPicture>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(ProgressPicture.GET_ALL_SQL, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            pics.add(makeProgressPicture(res));
            res.moveToNext();
        }
        return pics;
    }

    private ProgressPicture makeProgressPicture(Cursor res) {
        double pounds = res.getDouble(res.getColumnIndex(ProgressPicture.POUNDS_COLUMN_NAME));
        String date = res.getString(res.getColumnIndex(ProgressPicture.DATE_COLUMN_NAME));
        int time = res.getInt(res.getColumnIndex(ProgressPicture.TIME_COLUMN_NAME));
        String filepath = res.getString(res.getColumnIndex(ProgressPicture.PATH_COLUMN_NAME));
        int id = res.getInt(res.getColumnIndex(ProgressPicture.ID_COLUMN_NAME));
        return new ProgressPicture(pounds, date, time, filepath, id);
    }
}
