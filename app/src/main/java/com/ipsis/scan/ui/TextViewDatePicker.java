package com.ipsis.scan.ui;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import com.ipsis.scan.R;

import java.util.Calendar;

/**
 * Created by pobouteau on 9/26/16.
 */

public class TextViewDatePicker extends TextView {

    private String mTitle;
    private int mYear;
    private int mMonth;
    private int mDay;

    private OnDateChangeListener mOnDateChangeListener;
    private OnClickListener mOnClickListener;

    public TextViewDatePicker(Context context) {
        super(context);

        init("");
    }

    public TextViewDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TextViewTimePicker);
        String title = typedArray.getString(R.styleable.TextViewTimePicker_dialogTitle);
        typedArray.recycle();

        init(title);
    }

    public TextViewDatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TextViewTimePicker);
        String title = typedArray.getString(R.styleable.TextViewTimePicker_dialogTitle);
        typedArray.recycle();

        init(title);
    }

    @TargetApi(21)
    public TextViewDatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TextViewTimePicker);
        String title = typedArray.getString(R.styleable.TextViewTimePicker_dialogTitle);
        typedArray.recycle();

        init(title);
    }

    public void init(String title) {
        Calendar time = Calendar.getInstance();

        setTitle(title);

        setDate(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DAY_OF_MONTH));

        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = mYear;
                int month = mMonth;
                int day = mDay;

                if (year == -1 || month == -1 || day == -1) {
                    Calendar time = Calendar.getInstance();

                    year = time.get(Calendar.YEAR);
                    month = time.get(Calendar.MONTH);
                    day = time.get(Calendar.DAY_OF_MONTH);
                }

                DatePickerDialog datePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        setDate(year, month, dayOfMonth);
                    }
                }, year, month, day);
                datePicker.setTitle(mTitle);
                datePicker.show();
            }
        };
        setOnClickListener(mOnClickListener);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDate(int year, int month, int day) {
        mYear = year;
        mMonth = month;
        mDay = day;

        if (mYear != -1 && mMonth != -1 && mDay != -1) {
            setText((mDay < 10 ? "0" + mDay : mDay) + "/" + ((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1)) + "/" + mYear);
        } else {
            setText("-");
        }

        if (mOnDateChangeListener != null) {
            mOnDateChangeListener.onDateChanged(this);
        }
    }

    public int getYear() {
        return mYear;
    }

    public int getMonth() {
        return mMonth;
    }

    public int getDay() {
        return mDay;
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

    public void setOnDateChangeListener(OnDateChangeListener onDateChangeListener) {
        mOnDateChangeListener = onDateChangeListener;
    }

    public interface OnDateChangeListener {
        void onDateChanged(TextViewDatePicker view);
    }
}
