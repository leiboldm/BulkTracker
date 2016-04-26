package com.mattleibold.bulktracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BTLOG", "BootReceiver received!");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Utilities.setNotificationAlarm(context);
        }
    }
}
