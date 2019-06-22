package com.brouken.fixer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Shortcuts {

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
