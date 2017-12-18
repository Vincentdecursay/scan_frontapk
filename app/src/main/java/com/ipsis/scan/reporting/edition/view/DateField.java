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
import com.ipsis.scan.ui.TextViewDatePicker;

import static com.ipsis.scan.utils.AppUtils.dpToPx;
import static com.ipsis.scan.utils.AppUtils.setLinearLayoutParams;
import static com.ipsis.scan.utils.AppUtils.setRelativeLayoutParams;

/**
 * Created by pobouteau on 9/30/16.
 */

public class DateField extends ViewField {

    private TextViewDatePicker mDatePicker;

    public DateField(MissionField field, MissionValue value) {
        super(field, value);
    }

    @Override
    public void appendView(Context context, RelativeLayout layout) {
        LinearLayout inputLayout = new LinearLayout(context);
        setRelativeLayoutParams(inputLayout, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 52, 0, 44, 0);
        inputLayout.setOrientation(LinearLayout.VERTICAL);

        TextView labelTextView = new TextView(context);
        setLinearLayoutParams(labelTextView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 0, 0, 0);
        labelTextView.setText(mField.getLabel());
        labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        labelTextView.setTextColor(Color.parseColor("#9e9e9e"));

        mDatePicker = new TextViewDatePicker(context);
        //setRelativeLayoutParams(mDatePicker, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 52, 0, 0, 0);
        setLinearLayoutParams(mDatePicker, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 7, 0, 6);
        //mDatePicker.setMinHeight(dpToPx(context, 40));
        mDatePicker.setGravity(Gravity.CENTER_VERTICAL);
        mDatePicker.setHint(R.string.field_select_hint);
        mDatePicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mDatePicker.setTextColor(Color.BLACK);
        mDatePicker.setOnDateChangeListener(new TextViewDatePicker.OnDateChangeListener() {
            @Override
            public void onDateChanged(TextViewDatePicker view) {
                updateValue();
            }
        });

        updateView();

        inputLayout.addView(labelTextView);
        inputLayout.addView(mDatePicker);

        layout.addView(inputLayout);
    }

    @Override
    public void updateValue() {
        mValue.setValue((mDatePicker.getMonth() + 1) + "/" + mDatePicker.getDay() + "/" + mDatePicker.getYear());
    }

    @Override
    public void updateView() {
        String[] tokens = mValue.getValue().split("/");
        try {
            mDatePicker.setDate(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[0]) - 1, Integer.parseInt(tokens[1]));
        } catch (Exception e) {
            mDatePicker.setText("");
        }
    }

    @Override
    public void updateEnabled(boolean enabled) {
        mDatePicker.setEnabled(enabled);
    }
}
