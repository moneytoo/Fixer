package com.brouken.fixer.feature;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.brouken.fixer.Prefs;

import static com.brouken.fixer.Utils.log;

public class AppBackupReceiver extends BroadcastReceiver {

    // TODO: register receiver at runtime (when targeting O+)
    @Override
    public void onReceive(Context context, Intent intent) {
        Prefs prefs = new Prefs(context);
        if (prefs.isAppBackupEnabled()) {
            String pkg = intent.getData().getSchemeSpecificPart();
            log(pkg);

            AppBackup appBackup = new AppBackup(context);
            appBackup.execute(pkg);
        }
    }
}
