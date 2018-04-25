package com.brouken.fixer.feature;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FreezerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Freezer.launchAppIfEnabled(context, intent.getData().getSchemeSpecificPart());
    }
}
