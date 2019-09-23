package com.brouken.fixer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Shortcuts {

    public static void startRadio(Context context) {
        startSettingsActivity(context, "com.android.settings.RadioInfo");
    }

    private static void startSettingsActivity(Context context, final String activityName) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.android.settings",activityName));
        try {
            context.startActivity(intent);
        } catch (Exception x) {
            Toast.makeText(context, "No activity found", Toast.LENGTH_SHORT).show();
        }
    }
}
