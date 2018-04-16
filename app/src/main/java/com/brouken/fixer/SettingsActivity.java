package com.brouken.fixer;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.util.List;

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
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setComponent(new ComponentName("com.android.phone","com.android.phone.settings.PhoneAccountSettingsActivity"));
                    try {
                        startActivity(intent);
                    } catch (Exception x) {
                        try {
                            intent.setComponent(new ComponentName("com.android.phone","com.android.phone.CallFeaturesSetting"));
                            startActivity(intent);
                        } catch (Exception xx) {
                            Toast.makeText(getActivity(), "No activity found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });

            Preference radioPreference = findPreference("pref_radio");
            radioPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setComponent(new ComponentName("com.android.settings","com.android.settings.RadioInfo"));
                    try {
                        startActivity(intent);
                    } catch (Exception x) {
                        Toast.makeText(getActivity(), "No activity found", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            Preference gmsPreference = findPreference("pref_gms_location_persistent");
            gmsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP).add(
                                new Command(0, false, "pm disable com.google.android.gms/com.google.android.location.settings.LocationSettingsCheckerActivity") {
                                    String output = "";

                                    @Override
                                    public void commandCompleted(int id, int exitcode) {
                                        super.commandCompleted(id, exitcode);

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                builder.setMessage(output);
                                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.cancel();
                                                            }
                                                        });
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void commandOutput(int id, String line) {
                                        super.commandOutput(id, line);
                                        output += line + "\n";
                                    }
                                });
                    } catch (Exception x) {

                    }
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
                        packageName.equals("com.alibaba.aliexpresshd")) {
                    Preference preference = createToggle(packageName, packageInfo.applicationInfo.loadLabel(getActivity().getPackageManager()).toString());
                    preferenceCategory.addPreference(preference);
                }
            }
        }

        Preference createToggle(String pkg, String name) {
            Preference preference = new Preference(getContext());
            preference.setKey(pkg);
            preference.setTitle(name);

            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ComponentName adminComponentName = new ComponentName(getActivity().getApplicationContext(), AdminReceiver.class);
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);

                    boolean isAppHidden = devicePolicyManager.isApplicationHidden(adminComponentName, preference.getKey());
                    devicePolicyManager.setApplicationHidden(adminComponentName, preference.getKey(), !isAppHidden);

                    return true;
                }
            });

            return preference;
        }
    }
}

