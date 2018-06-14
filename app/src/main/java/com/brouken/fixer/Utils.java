package com.brouken.fixer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

public class Utils {

    public static void log(String text) {
        if (BuildConfig.DEBUG && text != null)
            Log.d("Fixer", text);
    }

    // https://www.xda-developers.com/how-to-automatically-disable-the-high-volume-warning-without-root/

    static boolean hasPermission(Context context, String permission) {
        return context.checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

    public static void setNoSafeVolume(Context context) {
        //log("setNoSafeVolume");

        // adb shell pm grant com.brouken.fixer android.permission.WRITE_SECURE_SETTINGS
        // settings get global audio_safe_volume_state
        if (hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            try {
                Settings.Global.putInt(context.getContentResolver(), "audio_safe_volume_state", 2);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void changeIME(Context context, boolean temporaryIME) {
        // https://stackoverflow.com/questions/11036435/switch-keyboard-profile-programmatically
        // ime list -s
        // com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME
        // org.pocketworkstation.pckeyboard/.LatinIME

        // ime set com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME

        // settings get secure default_input_method
        // settings put secure default_input_method org.pocketworkstation.pckeyboard/.LatinIME

        log("changeIME()");

        String ime = "org.pocketworkstation.pckeyboard/.LatinIME";

        if (!temporaryIME)
            ime = "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME";

        try {
            if (hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
                Settings.Secure.putString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD, ime);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
