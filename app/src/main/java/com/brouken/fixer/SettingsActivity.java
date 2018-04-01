package com.brouken.fixer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

public class SettingsActivity extends PreferenceActivity {

    //private static EditTextPreference customEnginePreference;

    /*
    private static Preference.OnPreferenceChangeListener sPreferenceListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

                if (preference.getKey().equals("engine")) {
                    if (stringValue.equals("custom"))
                        customEnginePreference.setEnabled(true);
                    else {
                        customEnginePreference.setEnabled(false);
                    }
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void listenPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(sPreferenceListener);

        // Trigger the listener immediately with the preference's current value.
        sPreferenceListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }
    */

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

            /*
            customEnginePreference = (EditTextPreference) findPreference("engine_custom");

            listenPreference(findPreference("engine"));
            listenPreference(findPreference("engine_custom"));

            Preference testPreference = findPreference("engine_test");
            testPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, Resources.getSystem().getString(android.R.string.ok));
                    startActivity(intent);

                    return true;
                }
            });
            */

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

            Preference gmsPreference = findPreference("pref_gms_location");
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
    }
}

