package com.ipsis.scan.settings;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.communication.service.SynchronisationClient;
import com.ipsis.scan.reporting.data.CacheData;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.security.locking.LockManager;
import com.ipsis.scan.ui.TimePickerPreference;
import com.ipsis.scan.utils.AppUtils;

import java.util.Calendar;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        Log.i("activitest", "activité " + this.getLocalClassName()  );

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        private SynchronisationClient mSynchronisationClient;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            mSynchronisationClient = new SynchronisationClient(getActivity());

            final CacheData cacheData = CacheManager.getInstance().getData();

            final TimePickerPreference startingHourPreference = (TimePickerPreference) findPreference("starting_hour");
            startingHourPreference.setTime(cacheData.getStartDate().get(Calendar.HOUR_OF_DAY), cacheData.getStartDate().get(Calendar.MINUTE));
            startingHourPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    int startingHour = startingHourPreference.getHour();
                    int startingMinute = startingHourPreference.getMinute();
                    int endingHour = cacheData.getEndDate().get(Calendar.HOUR_OF_DAY);
                    int endingMinute = cacheData.getEndDate().get(Calendar.MINUTE);

                    if (startingHour * 60 + startingMinute < endingHour * 60 + endingMinute) {
                        cacheData.getStartDate().set(Calendar.HOUR_OF_DAY, startingHour);
                        cacheData.getStartDate().set(Calendar.MINUTE, startingMinute);

                        CacheManager.getInstance().saveCache(getActivity());
                    } else {
                        startingHourPreference.setTime(cacheData.getStartDate().get(Calendar.HOUR_OF_DAY), cacheData.getStartDate().get(Calendar.MINUTE));

                        AppUtils.showErrorDialog(getActivity(), R.string.initialization_error_ending_hour);
                    }

                    return true;
                }
            });

            final TimePickerPreference endingHourPreference = (TimePickerPreference) findPreference("ending_hour");
            endingHourPreference.setTime(cacheData.getEndDate().get(Calendar.HOUR_OF_DAY), cacheData.getEndDate().get(Calendar.MINUTE));
            endingHourPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    int startingHour = cacheData.getStartDate().get(Calendar.HOUR_OF_DAY);
                    int startingMinute = cacheData.getStartDate().get(Calendar.MINUTE);
                    int endingHour = endingHourPreference.getHour();
                    int endingMinute = endingHourPreference.getMinute();

                    if (startingHour * 60 + startingMinute < endingHour * 60 + endingMinute) {
                        cacheData.getEndDate().set(Calendar.HOUR_OF_DAY, endingHourPreference.getHour());
                        cacheData.getEndDate().set(Calendar.MINUTE, endingHourPreference.getMinute());

                        CacheManager.getInstance().saveCache(getActivity());

                        mSynchronisationClient.scheduleSynchronisation();
                    } else {
                        endingHourPreference.setTime(cacheData.getEndDate().get(Calendar.HOUR_OF_DAY), cacheData.getEndDate().get(Calendar.MINUTE));

                        AppUtils.showErrorDialog(getActivity(), R.string.initialization_error_ending_hour);
                    }

                    return true;
                }
            });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            mSynchronisationClient.disconnect();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LockManager.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LockManager.getInstance().onPause(this);
    }
}
