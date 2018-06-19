package com.brouken.fixer;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.brouken.fixer.feature.AppBackup;
import com.brouken.fixer.feature.Freezer;

import java.util.List;

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

            Preference sipPreference = findPreference("pref_sip");
            sipPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Shortcuts.startSIP(getContext());
                    return true;
                }
            });

            Preference radioPreference = findPreference("pref_radio");
            radioPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Shortcuts.startRadio(getContext());
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
                    //startActivityForResult(intent, 0);
                    startActivity(intent);
                    return true;
                }
            });

            EditTextPreference sammyDNSPreference = (EditTextPreference) findPreference("pref_sammy_dns");
            sammyDNSPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String dns = (String) newValue;
                    Sammy.setDNS(getActivity(), dns);
                    return true;
                }
            });

            EditTextPreference sammyKeyPreference = (EditTextPreference) findPreference("pref_sammy_key");
            sammyKeyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Sammy.setKey(getActivity(), (String) newValue);
                    return true;
                }
            });

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (sharedPreferences.contains("sammy_license"))
                sammyKeyPreference.setSummary("License status: " + sharedPreferences.getString("sammy_license", "unknown"));

            addToggles();

            registerSwitchChangeToServiceUpdate("pref_side_screen_gestures");

            SwitchPreference appBackupPreference = (SwitchPreference) findPreference("pref_app_backup");
            appBackupPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enable = (boolean) newValue;
                    log("app backup: onPreferenceChange " + enable);

                    if (enable) {
                        AppBackup.setup(getActivity());
                    } else {
                        AppBackup.unschedule(getContext());
                    }

                    return true;
                }
            });

            SwitchPreference sammyWakeupOnPowerStatePreference = (SwitchPreference) findPreference("pref_sammy_power_wakeup");
            sammyWakeupOnPowerStatePreference.setChecked(Sammy.isWakeupOnPowerStateEnabled());
            sammyWakeupOnPowerStatePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enable = (boolean) newValue;
                    Sammy.setWakeupOnPowerState(enable);
                    return true;
                }
            });
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
            }/* else if (id == R.id.root) {
                RootTools.isAccessGiven();
            }*/
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

        void addToggles() {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("screen");
            PreferenceCategory preferenceCategory = new PreferenceCategory(getContext());
            preferenceCategory.setTitle("Enable/disable apps");

            preferenceScreen.addPreference(preferenceCategory);

            List<PackageInfo> packageInfos = getActivity().getPackageManager().getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            for (PackageInfo packageInfo : packageInfos) {
                final String packageName = packageInfo.packageName;
                if (packageName.equals("com.pandora.android") ||
                        packageName.equals("com.xiaomi.hm.health") ||
                        packageName.equals("com.spotify.music") ||
                        packageName.equals("com.alibaba.aliexpresshd")) {
                    Preference preference = createToggle(packageName, packageInfo.applicationInfo.loadLabel(getActivity().getPackageManager()).toString());
                    preferenceCategory.addPreference(preference);
                    ((SwitchPreference)preference).setChecked(packageInfo.applicationInfo.enabled);
                }
            }
        }

        Preference createToggle(String pkg, String name) {
            SwitchPreference switchPreference = new SwitchPreference(getContext());
            switchPreference.setKey(pkg);
            switchPreference.setTitle(name);

            switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ((SwitchPreference) preference).setChecked(Freezer.switchAppState(getContext(), preference.getKey()));
                    return true;
                }
            });

            return switchPreference;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppBackup.REQUEST_SD_ACCESS && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            log(uri.toString());
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Prefs prefs = new Prefs(this);
            prefs.setSdRoot(uri.toString());
            AppBackup.schedule(this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

