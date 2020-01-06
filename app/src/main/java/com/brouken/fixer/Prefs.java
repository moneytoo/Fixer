package com.brouken.fixer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    SharedPreferences mSharedPreferences;

    private boolean pref_keyboard_switching = false;
    private boolean pref_app_backup = false;
    private boolean pref_auto_select_client_certificate = false;
    private boolean pref_long_press_volume = false;
    private boolean pref_power_wakeup = false;

    private boolean pref_oneplus_call_recording = false;
    private boolean pref_oneplus_alert_slider_actions = false;

    public Prefs(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadSavedPreferences();
    }

    private void loadSavedPreferences() {
        pref_keyboard_switching = mSharedPreferences.getBoolean("pref_keyboard_switching", pref_keyboard_switching);
        pref_app_backup = mSharedPreferences.getBoolean("pref_app_backup", pref_app_backup);
        pref_auto_select_client_certificate = mSharedPreferences.getBoolean("pref_auto_select_client_certificate", pref_auto_select_client_certificate);
        pref_long_press_volume = mSharedPreferences.getBoolean("pref_long_press_volume", pref_long_press_volume);
        pref_power_wakeup = mSharedPreferences.getBoolean("pref_power_wakeup", pref_power_wakeup);

        pref_oneplus_call_recording = mSharedPreferences.getBoolean("pref_oneplus_call_recording", pref_oneplus_call_recording);
        pref_oneplus_alert_slider_actions = mSharedPreferences.getBoolean("pref_oneplus_alert_slider_actions", pref_oneplus_alert_slider_actions);
    }

    public boolean isKeyboardSwitchingEnabled() {
        return pref_keyboard_switching;
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

    public boolean isPowerWakeupEnabled() {
        return pref_power_wakeup;
    }

    public boolean isOnePlusCallRecordingEnabled() {
        return pref_oneplus_call_recording;
    }

    public boolean isOnePlusAlertSliderActionsEnabled() {
        return pref_oneplus_alert_slider_actions;
    }
}
