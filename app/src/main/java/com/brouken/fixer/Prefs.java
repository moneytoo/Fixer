package com.brouken.fixer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    SharedPreferences mSharedPreferences;

    private boolean pref_no_safe_volume_warning = false;
    private boolean pref_keyboard_switching = false;
    private boolean pref_gms_location = false;
    private boolean pref_side_screen_gestures = false;
    private boolean pref_samsung_led_dnd = false;
    private boolean pref_samsung_popups = false;
    private boolean pref_app_backup = false;
    private boolean pref_auto_select_client_certificate = false;

    private String pref_sd_root = "";

    public Prefs(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadSavedPreferences();
    }

    private void loadSavedPreferences() {
        pref_no_safe_volume_warning = mSharedPreferences.getBoolean("pref_no_safe_volume_warning", pref_no_safe_volume_warning);
        pref_keyboard_switching = mSharedPreferences.getBoolean("pref_keyboard_switching", pref_keyboard_switching);
        pref_gms_location = mSharedPreferences.getBoolean("pref_gms_location", pref_gms_location);
        pref_side_screen_gestures = mSharedPreferences.getBoolean("pref_side_screen_gestures", pref_side_screen_gestures);
        pref_samsung_led_dnd = mSharedPreferences.getBoolean("pref_samsung_led_dnd", pref_samsung_led_dnd);
        pref_samsung_popups = mSharedPreferences.getBoolean("pref_samsung_popups", pref_samsung_popups);
        pref_app_backup = mSharedPreferences.getBoolean("pref_app_backup", pref_app_backup);
        pref_auto_select_client_certificate = mSharedPreferences.getBoolean("pref_auto_select_client_certificate", pref_auto_select_client_certificate);

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

    public boolean isGMSNoLocationEnabled() {
        return pref_gms_location;
    }

    public boolean isSideScreenGesturesEnabled() {
        return pref_side_screen_gestures;
    }

    public boolean isSamsungNoLedInDnDEnabled() {
        return pref_samsung_led_dnd;
    }

    public boolean isSamsungNoPopupsEnabled() {
        return pref_samsung_popups;
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
}
