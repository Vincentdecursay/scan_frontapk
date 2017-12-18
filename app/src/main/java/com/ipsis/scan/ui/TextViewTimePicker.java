package com.ipsis.scan.ui;

import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import com.ipsis.scan.R;
import com.ipsis.scan.utils.DateUtils;

import java.util.Calendar;

/**
 * Created by pobouteau on 9/26/16.
 */

public class TextViewTimePicker extends TextView {

    private String mTitle;
    private int mHour;
    private int mMinute;

    private OnTimeChangeListener mOnTimeChangeListener;
    private OnClickListener mOnClickListener;

    public TextViewTimePicker(Context context) {
        super(context);

        init("");
    }

    public TextViewTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TextViewTimePicker);
        String title = typedArray.getString(R.styleable.TextViewTimePicker_dialogTitle);
        typedArray.recycle();

        init(title);
    }

    public TextViewTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TextViewTimePicker);
        String title = typedArray.getString(R.styleable.TextViewTimePicker_dialogTitle);
        typedArray.recycle();

        init(title);
    }

    @TargetApi(21)
    public TextViewTimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TextViewTimePicker);
        String title = typedArray.getString(R.styleable.TextViewTimePicker_dialogTitle);
        typedArray.recycle();

        init(title);
    }

    public void init(String title) {
        Calendar time = Calendar.getInstance();

        setTitle(title);

        setTime(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));

        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    }
                }, hour, minute, true);
                timePicker.setTitle(mTitle);
                timePicker.show();
            }
        };
        setOnClickListener(mOnClickListener);
    }

    public void setToNow() {
        Calendar time = Calendar.getInstance();

        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        setTime(hour, minute);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTime(int hour, int minute) {
        String oldTime = DateUtils.formatDate(mHour, mMinute);
        String newTime = DateUtils.formatDate(hour, minute);
        int oldHour = mHour;
        int oldMinute = mMinute;

        mHour = hour;
        mMinute = minute;
        if (mHour != -1 && mMinute != -1) {
            setText(mHour + ":" + (mMinute < 10 ? "0" + mMinute : mMinute));
        } else {
            setText("-");
        }

        if (mOnTimeChangeListener != null) {
            if (!mOnTimeChangeListener.onTimeChanged(this, oldTime, newTime)) {
                mHour = oldHour;
                mMinute = oldMinute;
                if (mHour != -1 && mMinute != -1) {
                    setText(mHour + ":" + (mMinute < 10 ? "0" + mMinute : mMinute));
                } else {
                    setText("-");
                }
            }
        }
    }

    public void setTime(String time) {
        if (time.isEmpty()) {
            Calendar date = Calendar.getInstance();
            setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));
        } else {
            String[] tokens = time.split(":");
            try {
                setTime(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
            } catch (Exception e) {

            }
        }
    }

    public int getHour() {
        return mHour;
    }

    public int getMinute() {
        return mMinute;
    }

    public void setOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
        mOnTimeChangeListener = onTimeChangeListener;
    }

    public boolean isEmpty() {
        return getText().toString().equals("-");
    }

    @Override
    public boolean isEnabled() {
        return super.hasOnClickListeners();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            setOnClickListener(mOnClickListener);
        } else {
            setOnClickListener(null);
        }
    }

    public interface OnTimeChangeListener {
        boolean onTimeChanged(TextViewTimePicker view, String oldTime, String newTime);
    }
}
