package com.mattleibold.bulktracker;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.settings_layout);
    }

    public static final String REMINDERS_KEY = "pref_reminders";
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.d("BTLOG", "SharedPreference changed with key " + key);
        if (key.equals(REMINDERS_KEY)) {
            boolean reminders = sharedPreferences.getBoolean(key, true);
            Log.d("BTLOG", "Reminders set to " + reminders);
            if (reminders) {
                Utilities.refreshNotificationAlarm(getActivity());
            } else {
                Utilities.cancelPreviousNotifications(getActivity());
                Utilities.removeNotificationAlarm(getActivity());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
