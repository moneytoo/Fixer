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

    public static boolean isAccessibilitySettingsEnabled(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        final TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            final String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    final String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.startsWith(context.getPackageName() + "/")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isNotificationServiceEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
