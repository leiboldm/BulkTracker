package com.mattleibold.bulktracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        context = context.getApplicationContext();
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    // Schedules an alarm to send a notification
    public static void setNotificationAlarm(Context context) {
        if (!notificationsActive(context)) return;
        context = context.getApplicationContext();
        PendingIntent pendingIntent = getAlarmReceiverPI(context);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = AlarmManager.INTERVAL_DAY; // milliseconds between alarms
        alarmMgr.setInexactRepeating(AlarmManager.RTC,
                System.currentTimeMillis() + interval,
                interval, pendingIntent);
    }

    public static void removeNotificationAlarm(Context context) {
        context = context.getApplicationContext();
        PendingIntent pendingIntent = getAlarmReceiverPI(context);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendingIntent);
    }

    // removes any previous notification alarms, and sets a new one
    public static void refreshNotificationAlarm(Context context) {
        removeNotificationAlarm(context);
        setNotificationAlarm(context);
    }

    public static void cancelPreviousNotifications(Context context) {
        context = context.getApplicationContext();
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    // Check the app settings to see if the user has enabled notifications
    public static boolean notificationsActive(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(SettingsFragment.REMINDERS_KEY, true);
    }

    public static boolean handleOptionsItemSelected(Context context, int id) {
        if (id == R.id.action_settings) {
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity(intent);
        } else if (id == R.id.action_weight_entry) {
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

    // Loads the image at filepath into imageView.  If square is true, it crops to image to a square
    public static void loadBitmap(ImageView imageView, String filepath, boolean square) {
        ImageLoaderTask task = new ImageLoaderTask(imageView, filepath, square);
        task.execute(0);
    }

    // overload of loadBitmap that defaults to retaining original image aspect ratio
    public static void loadBitmap(ImageView imageView, String filepath) {
        loadBitmap(imageView, filepath, false);
    }

    public static void hideStatusBar(Activity context) {
        Window window = context.getWindow();
        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = window.getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    // Delete a picture from the filesystem and database
    public static boolean deleteProgressPicture(Context context, ProgressPicture photo) {
        DBHelper db = new DBHelper(context);
        boolean success = db.deleteProgressPicture(photo.id);
        if (!success) Log.d("BTLOG", "Failure deleting " + photo.filepath + " from database");
        success = success && deletePhoto(context, photo.filepath);
        return success;
    }

    // Delete a photo from the filesystem and remove it from the Gallery
    public static boolean deletePhoto(Context context, String filepath) {
        Log.d("BTLOG", "Deleting " + filepath);
        File file = new File(filepath);
        if (!file.exists()) {
            Log.d("BTLOG", "File " + filepath + " doesn't exist");
        }
        boolean success = file.delete();
        if (!success) Log.d("BTLOG", "Failure deleting " + filepath + " from filesystem");
        else {
            String[] projection = { MediaStore.Images.Media._ID };

            // Match on the file path
            String selection = MediaStore.Images.Media.DATA + " = ?";
            String[] selectionArgs = new String[] { file.getAbsolutePath() };

            // Query for the ID of the media matching the file path
            Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = context.getContentResolver();
            Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
            if (c.moveToFirst()) {
                // We found the ID. Deleting the item via the content provider will also remove the file
                long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                contentResolver.delete(deleteUri, null, null);
            } else {
                // File not found in media store DB
            }
            c.close();
        }
        return success;
    }

    public static String getWeightUnitStr(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(SettingsFragment.UNIT_KEY, "lbs");
    }

    public static final double lbsPerKg = 2.20462;
    public static double lbsToKgs(double lbs) {
        return round(lbs / lbsPerKg, 1);
    }
    public static double kgsToLbs(double kgs) {
        return round(kgs * lbsPerKg, 1);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
