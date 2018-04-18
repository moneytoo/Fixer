package com.brouken.fixer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Shortcuts {

    public static void startSIP(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.android.phone","com.android.phone.settings.PhoneAccountSettingsActivity"));
        try {
            context.startActivity(intent);
        } catch (Exception x) {
            try {
                intent.setComponent(new ComponentName("com.android.phone","com.android.phone.CallFeaturesSetting"));
                context.startActivity(intent);
            } catch (Exception xx) {
                Toast.makeText(context, "No activity found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void startRadio(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.android.settings","com.android.settings.RadioInfo"));
        try {
            context.startActivity(intent);
        } catch (Exception x) {
            Toast.makeText(context, "No activity found", Toast.LENGTH_SHORT).show();
        }
    }
}
