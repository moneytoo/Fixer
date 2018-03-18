package com.brouken.fixer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action == null) {
            Utils.setNoSafeVolume(context);
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent delayedIntent = new Intent(context, IntentReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, delayedIntent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 30000, pendingIntent);
        }
    }
}
