package com.brouken.fixer;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import static com.brouken.fixer.Utils.hasPermission;
import static com.brouken.fixer.Utils.log;

public class Interruption {

    public Interruption(Context context) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                log(intent.getAction());

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                int filter = notificationManager.getCurrentInterruptionFilter();
                log("filter=" + filter);

                if(filter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                    // Normal interruption filter - no notifications are suppressed
                    switchLed(context, 1);
                } else {
                    switchLed(context, 0);
                }
            }
        };

        context.registerReceiver(receiver, intentFilter);
    }

    private void switchLed(Context context, int state) {
        if (hasPermission(context, Manifest.permission.WRITE_SETTINGS)) {
            ContentResolver contentResolver = context.getContentResolver();
            try {
                Settings.System.putInt(contentResolver, "led_indicator_charing", state);
                Settings.System.putInt(contentResolver, "led_indicator_low_battery", state);
                Settings.System.putInt(contentResolver, "led_indicator_missed_event", state);
                Settings.System.putInt(contentResolver, "led_indicator_voice_recording", state);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (RootTools.isRootAvailable()) {
            try {
                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(new Command(0, false, "settings put system led_indicator_charing " + state));
                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(new Command(0, false, "settings put system led_indicator_low_battery " + state));
                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(new Command(0, false, "settings put system led_indicator_missed_event " + state));
                RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(new Command(0, false, "settings put system led_indicator_voice_recording " + state));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
