package com.brouken.fixer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    SharedPreferences mSharedPreferences;

    private boolean pref_no_safe_volume_warning = false;
    private boolean pref_keyboard_switching = false;
    private boolean pref_side_screen_gestures = false;
    private boolean pref_app_backup = false;
    private boolean pref_auto_select_client_certificate = false;
    private boolean pref_long_press_volume = false;

    private String pref_sd_root = "";

    public Prefs(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadSavedPreferences();
    }

    private void loadSavedPreferences() {
        pref_no_safe_volume_warning = mSharedPreferences.getBoolean("pref_no_safe_volume_warning", pref_no_safe_volume_warning);
        pref_keyboard_switching = mSharedPreferences.getBoolean("pref_keyboard_switching", pref_keyboard_switching);
        pref_side_screen_gestures = mSharedPreferences.getBoolean("pref_side_screen_gestures", pref_side_screen_gestures);
        pref_app_backup = mSharedPreferences.getBoolean("pref_app_backup", pref_app_backup);
        pref_auto_select_client_certificate = mSharedPreferences.getBoolean("pref_auto_select_client_certificate", pref_auto_select_client_certificate);
        pref_long_press_volume = mSharedPreferences.getBoolean("pref_long_press_volume", pref_long_press_volume);

        pref_sd_root = mSharedPreferences.getString("pref_sd_root", pref_sd_root);
    }

    public void setSdRoot(String root) {
        pref_sd_root = root;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("pref_sd_root", pref_sd_root);
        editor.apply();
    }

    public boolean isNoSafeVolumeWarningEnabled() {
        return pref_no_safe_volume_warning;
    }

    public boolean isKeyboardSwitchingEnabled() {
        return pref_keyboard_switching;
    }

    public boolean isSideScreenGesturesEnabled() {
        return pref_side_screen_gestures;
    }

    public String getSdRoot() {
        return pref_sd_root;
    }

    public boolean isAppBackupEnabled() {
        return pref_app_backup;
    }

    public boolean isAutoSelectClientCertificateEnabled() {
        return pref_auto_select_client_certificate;
    }

    public boolean isLongPressVolumeEnabled() {
        return pref_long_press_volume;
    }
}
