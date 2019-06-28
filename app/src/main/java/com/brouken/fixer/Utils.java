package com.brouken.fixer;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
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

    public static void setEnableCallRecording(Context context) {
        // adb shell settings put global op_voice_recording_supported_by_mcc 1
        if (hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)) {
            try {
                Settings.Global.putInt(context.getContentResolver(), "op_voice_recording_supported_by_mcc", 1);
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

    public static boolean isAccessibilitySettingsEnabled(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + MonitorService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isDeviceAdminEnabled(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName deviceAdmin = new ComponentName(context.getApplicationContext(), AdminReceiver.class);
        return devicePolicyManager.isAdminActive(deviceAdmin);
    }
}
