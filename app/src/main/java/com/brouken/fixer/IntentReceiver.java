package com.brouken.fixer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.brouken.fixer.Utils.log;

public class IntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        log(action);

        if (action == null) {
            Prefs prefs = new Prefs(context);
            if (prefs.isNoSafeVolumeWarningEnabled())
                Utils.setNoSafeVolume(context);
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent delayedIntent = new Intent(context, IntentReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, delayedIntent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 30000, pendingIntent);
        }
    }
}
