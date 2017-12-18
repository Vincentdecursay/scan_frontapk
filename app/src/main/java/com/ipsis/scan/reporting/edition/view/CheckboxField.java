package com.ipsis.scan.reporting.edition.view;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionValue;

import static com.ipsis.scan.utils.AppUtils.dpToPx;
import static com.ipsis.scan.utils.AppUtils.setRelativeLayoutParams;

/**
 * Created by pobouteau on 9/30/16.
 */

public class CheckboxField extends ViewField {

    private AppCompatCheckBox mCheckBox;

    public CheckboxField(MissionField field, MissionValue value) {
        super(field, value);
    }

    @Override
    public void appendView(Context context, RelativeLayout layout) {
        mCheckBox = new AppCompatCheckBox(context);
        setRelativeLayoutParams(mCheckBox, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 46, 0, 0, 0);
        mCheckBox.setMinHeight(dpToPx(context, 40));
        mCheckBox.setText(getNoHeaderLabel());
        mCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateValue();
            }
        });

        updateView();

        layout.addView(mCheckBox);
    }

    @Override
    public void updateValue() {
        mValue.setValue(mCheckBox.isChecked() ? "true" : "false");

        if (mCheckBox.isChecked()) {
            setHideGroupVisibility(View.VISIBLE);
        } else {
            setHideGroupVisibility(View.GONE);
        }
    }

    @Override
    public void updateView() {
        if (mValue.getValue().equals("true")) {
            mCheckBox.setChecked(true);
        }

        if (mCheckBox.isChecked()) {
            setHideGroupVisibility(View.VISIBLE);
        } else {
            setHideGroupVisibility(View.GONE);
        }
    }

    @Override
    public void updateEnabled(boolean enabled) {
        mCheckBox.setEnabled(enabled);
    }
}
