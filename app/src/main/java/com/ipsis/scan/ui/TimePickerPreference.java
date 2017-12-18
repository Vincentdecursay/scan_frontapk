package com.ipsis.scan.ui;


import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by pobouteau on 10/6/16.
 */

public class TimePickerPreference extends Preference {

    private int mHour;
    private int mMinute;

    public TimePickerPreference(Context context) {
        super(context);

        init();
    }

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TimePickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @TargetApi(21)
    public TimePickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        Calendar time = Calendar.getInstance();
        setTime(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));

        setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int hour = mHour;
                int minute = mMinute;

                if (hour == -1 || minute == -1) {
                    Calendar time = Calendar.getInstance();

                    hour = time.get(Calendar.HOUR_OF_DAY);
                    minute = time.get(Calendar.MINUTE);
                }

                TimePickerDialog timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        setTime(selectedHour, selectedMinute);

                        if (getOnPreferenceChangeListener() != null) {
                            getOnPreferenceChangeListener().onPreferenceChange(TimePickerPreference.this, TimePickerPreference.this);
                        }
                    }
                }, hour, minute, true);
                timePicker.setTitle(getTitle());
                timePicker.show();

                return true;
            }
        });
    }

    public void setTime(int hour, int minute) {
        mHour = hour;
        mMinute = minute;

        setSummary(getTime());
    }

    public void setTime(String time) {
        String[] tokens = time.split(":");
        try {
            setTime(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
        } catch (Exception e) {

        }
    }

    public int getHour() {
        return mHour;
    }

    public int getMinute() {
        return mMinute;
    }

    public String getTime() {
        if (mHour != -1 && mMinute != -1) {
            return mHour + ":" + (mMinute < 10 ? "0" + mMinute : mMinute);
        } else {
            return "-";
        }
    }
}
