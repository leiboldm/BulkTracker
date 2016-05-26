package com.mattleibold.bulktracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BTLOG", "Alarm received at " + Utilities.getSecondsSinceStartOfDay());
        Intent i = new Intent(context, NotificationService.class);
        context.startService(i);
    }
}
