package com.mattleibold.bulktracker;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationService extends IntentService {
    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("BTLOG", "Notification Service!");
        DBHelper db = new DBHelper(this);
        if (db.getWeightsCount() < 1) {
            return;
        }
        DBHelper.WeightEntry mostRecent = db.getMostRecentWeight();

        // only send notification if there hasn't been a new weight entered in the last wait_hours
        // hours
        int wait_hours = 24;
        if ((mostRecent.makeTimestamp() + wait_hours * 60 * 60)
                < (System.currentTimeMillis() / 1000)) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(getString(R.string.reminder_notification_title))
                            .setContentText(getString(R.string.reminder_notification_text));
            mBuilder.setVibrate(new long[]{500, 500, 500, 500, 500});
            mBuilder.setLights(0x97935d, 3000, 3000);
            Intent resultIntent = new Intent(this, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notification_id = mostRecent.makeTimestamp();
            mNotificationManager.notify(notification_id, mBuilder.build());
        }
    }
}
