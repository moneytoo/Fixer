package com.brouken.fixer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    SharedPreferences mSharedPreferences;

    private boolean pref_volume;
    private boolean pref_ime;

    public Prefs(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void loadSavedPreferences() {
        pref_volume = mSharedPreferences.getBoolean("pref_no_safe_volume_warning", pref_volume);
        pref_ime = mSharedPreferences.getBoolean("pref_keyboard_switching", pref_ime);
    }

    public boolean isNoSafeVolumeWarningEnabled() {
        loadSavedPreferences();
        return pref_volume;
    }

    public boolean isKeyboardSwitchingEnabled() {
        loadSavedPreferences();
        return pref_ime;
    }
}
