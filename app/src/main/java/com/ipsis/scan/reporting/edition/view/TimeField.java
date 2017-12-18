package com.ipsis.scan.reporting.edition.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionValue;
import com.ipsis.scan.ui.TextViewTimePicker;

import static com.ipsis.scan.utils.AppUtils.dpToPx;
import static com.ipsis.scan.utils.AppUtils.setLinearLayoutParams;
import static com.ipsis.scan.utils.AppUtils.setRelativeLayoutParams;

/**
 * Created by pobouteau on 9/30/16.
 */

public class TimeField extends ViewField {

    private TextViewTimePicker mTimePicker;

    public TimeField(MissionField field, MissionValue value) {
        super(field, value);
    }

    @Override
    public void appendView(Context context, RelativeLayout layout) {
        LinearLayout inputLayout = new LinearLayout(context);
        setRelativeLayoutParams(inputLayout, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 52, 0, 44, 0);
        inputLayout.setOrientation(LinearLayout.VERTICAL);

        TextView labelTextView = new TextView(context);
        setLinearLayoutParams(labelTextView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 0, 0, 0);
        labelTextView.setText(getNoHeaderLabel());
        labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        labelTextView.setTextColor(Color.parseColor("#9e9e9e"));

        mTimePicker = new TextViewTimePicker(context);
        //setRelativeLayoutParams(mTimePicker, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 52, 0, 0, 0);
        setLinearLayoutParams(mTimePicker, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 7, 0, 6);
        //mTimePicker.setMinHeight(dpToPx(context, 40));
        mTimePicker.setGravity(Gravity.CENTER_VERTICAL);
        mTimePicker.setHint(R.string.field_select_hint);
        mTimePicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mTimePicker.setTextColor(Color.BLACK);
        mTimePicker.setOnTimeChangeListener(new TextViewTimePicker.OnTimeChangeListener() {
            @Override
            public boolean onTimeChanged(TextViewTimePicker view, String oldTime, String newTime) {
                updateValue();

                return true;
            }
        });

        updateView();

        inputLayout.addView(labelTextView);
        inputLayout.addView(mTimePicker);

        layout.addView(inputLayout);
    }

    @Override
    public void updateValue() {
        int minute = mTimePicker.getMinute();

        mValue.setValue(mTimePicker.getHour() + ":" + (minute < 10 ? "0" + minute : minute));
    }

    @Override
    public void updateView() {
        String[] tokens = mValue.getValue().split(":");
        try {
            mTimePicker.setTime(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
        } catch (Exception e) {
            mTimePicker.setText("");
        }
    }

    @Override
    public void updateEnabled(boolean enabled) {
        mTimePicker.setEnabled(enabled);
    }

    public TextViewTimePicker getTimePicker() {
        return mTimePicker;
    }
}
