package com.brouken.fixer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.brouken.fixer.Utils.hasPermission;
import static com.brouken.fixer.Utils.log;

public class Prefs {

    SharedPreferences mSharedPreferences;

    private boolean pref_no_safe_volume_warning = true;
    private boolean pref_keyboard_switching = true;
    private boolean pref_media_volume_default = true;
    private boolean pref_power_display_off = true;
    private boolean pref_gms_location = true;
    private boolean pref_samsung_led_dnd = false;

    public Prefs(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadSavedPreferences();
    }

    private void loadSavedPreferences() {
        pref_no_safe_volume_warning = mSharedPreferences.getBoolean("pref_no_safe_volume_warning", pref_no_safe_volume_warning);
        pref_keyboard_switching = mSharedPreferences.getBoolean("pref_keyboard_switching", pref_keyboard_switching);
        pref_media_volume_default = mSharedPreferences.getBoolean("pref_media_volume_default", pref_media_volume_default);
        pref_power_display_off = mSharedPreferences.getBoolean("pref_power_display_off", pref_power_display_off);
        pref_gms_location = mSharedPreferences.getBoolean("pref_gms_location", pref_gms_location);
        pref_samsung_led_dnd = mSharedPreferences.getBoolean("pref_samsung_led_dnd", pref_samsung_led_dnd);
    }

    public boolean isNoSafeVolumeWarningEnabled() {
        return pref_no_safe_volume_warning;
    }

    public boolean isKeyboardSwitchingEnabled() {
        return pref_keyboard_switching;
    }

    public boolean isMediaVolumeDefaultEnabled() {
        return pref_media_volume_default;
    }

    public boolean isDisplayOffOnPowerEventsEnabled() {
        return pref_power_display_off;
    }

    public boolean isGMSNoLocationEnabled() {
        return pref_gms_location;
    }

    public boolean isSamsungNoLedInDnDEnabled() {
        return pref_samsung_led_dnd;
    }
}
