package com.brouken.fixer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Shortcuts {

    public static void startRadio(Context context) {
        startPhoneActivity(context, "com.android.phone.settings.RadioInfo");
    }

    private static void startPhoneActivity(Context context, final String activityName) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.android.phone",activityName));
        try {
            context.startActivity(intent);
        } catch (Exception x) {
            Toast.makeText(context, "No activity found", Toast.LENGTH_SHORT).show();
        }
    }
}
