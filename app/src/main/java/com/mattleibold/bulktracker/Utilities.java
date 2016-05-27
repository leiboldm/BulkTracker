package com.mattleibold.bulktracker;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.IOException;
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

    private static PendingIntent getAlarmReceiverPI(Context context) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    // Schedules an alarm to send a notification
    public static void setNotificationAlarm(Context context) {
        PendingIntent pendingIntent = getAlarmReceiverPI(context);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = AlarmManager.INTERVAL_DAY; // milliseconds between alarms
        alarmMgr.setInexactRepeating(AlarmManager.RTC,
                System.currentTimeMillis() - 1,
                interval, pendingIntent);
    }

    // removes any previous notification alarms, and sets a new one
    public static void refreshNotificationAlarm(Context context) {
        PendingIntent pendingIntent = getAlarmReceiverPI(context);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendingIntent);
        setNotificationAlarm(context);
    }

    public static void cancelPreviousNotifications(Context context) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    // returns a Bitmap containing the image in filepath rotated to the correct orientation
    // returns null if filepath cannot be decoded
    public static Bitmap loadBitmapWithRotation(String filepath) {
        Bitmap image = BitmapFactory.decodeFile(filepath);
        if (image == null) return null;

        float rotation = 0f;
        try {
            ExifInterface exifData = new ExifInterface(filepath);
            int exifRotation = exifData.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (exifRotation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270f;
                    break;
            }
        } catch (IOException exception) {
            Log.d("BTLOG", "IOException in ExifInterface: " + exception.getMessage());
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap rotated = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(),
                matrix, true);
        return rotated;
    }

    public static boolean handleOptionsItemSelected(Context context, int id) {
        if (id == R.id.action_weight_entry) {
            Intent intent = new Intent(context, WeightEntryActivity.class);
            context.startActivity(intent);
        } else if (id == R.id.action_history) {
            Intent intent = new Intent(context, WeightHistoryActivity.class);
            context.startActivity(intent);
        } else if (id == R.id.action_pictures) {
            Intent intent = new Intent(context, PictureGalleryActivity.class);
            context.startActivity(intent);
        } else if (id == R.id.action_graph) {
            Intent intent = new Intent(context, GraphViewActivity.class);
            context.startActivity(intent);
        }
        return true;
    }

}
