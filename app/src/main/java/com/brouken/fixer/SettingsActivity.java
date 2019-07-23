package com.brouken.fixer;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.brouken.fixer.feature.AppBackup;

import static com.brouken.fixer.Utils.log;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent.getStringArrayExtra(EXTRA_SHOW_FRAGMENT) == null) {
            getIntent().putExtra(EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onIsMultiPane() {
        return false;
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            Preference radioPreference = findPreference("pref_radio");
            radioPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Shortcuts.startRadio(getContext());
                    return true;
                }
            });

            Preference batteryOptimizationPreference = findPreference("pref_battery_optimization");
            batteryOptimizationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Shortcuts.startBatteryOptimization(getContext());
                    return true;
                }
            });

            Preference toggleImmersiveNavigationPreference = findPreference("pref_immersive_navigation");
            toggleImmersiveNavigationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utils.toggleImmersiveNavigation(getContext());
                    return true;
                }
            });

            Preference deviceAdminPreference = findPreference("pref_device_admin");
            deviceAdminPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ComponentName mDeviceAdmin;
                    mDeviceAdmin = new ComponentName(getActivity().getApplicationContext(), AdminReceiver.class);

                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
                    startActivity(intent);
                    return true;
                }
            });

            registerSwitchChangeToServiceUpdate("pref_side_screen_gestures");

            SwitchPreference appBackupPreference = (SwitchPreference) findPreference("pref_app_backup");
            appBackupPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enable = (boolean) newValue;
                    log("app backup: onPreferenceChange " + enable);

                    if (enable) {
                        AppBackup.schedule(getContext());
                    } else {
                        AppBackup.unschedule(getContext());
                    }

                    return true;
                }
            });

            final boolean removeAccessibility = Utils.isAccessibilitySettingsEnabled(this.getContext());
            final boolean removeAdmin = Utils.isDeviceAdminEnabled(this.getContext());

            PreferenceCategory categorySetup = (PreferenceCategory) findPreference("pref_setup");

            if (removeAccessibility)
                categorySetup.removePreference(findPreference("pref_accessibility"));
            if (removeAdmin)
                categorySetup.removePreference(findPreference("pref_device_admin"));

            if (removeAccessibility && removeAdmin) {
                PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("screen");
                preferenceScreen.removePreference(findPreference("pref_setup"));
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            //inflater.inflate(R.menu.menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        void registerSwitchChangeToServiceUpdate(String pref) {
            SwitchPreference switchPreference = (SwitchPreference) findPreference(pref);
            switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    log("onPreferenceChange");
                    getActivity().startService(new Intent(getActivity(), MonitorService.class));
                    return true;
                }
            });
        }
    }
}

