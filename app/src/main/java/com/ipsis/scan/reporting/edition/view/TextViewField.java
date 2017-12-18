package com.ipsis.scan.reporting.edition.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionValue;

import static com.ipsis.scan.utils.AppUtils.dpToPx;
import static com.ipsis.scan.utils.AppUtils.setRelativeLayoutParams;

/**
 * Created by pobouteau on 9/30/16.
 */

public class TextViewField extends ViewField {

    public TextViewField(MissionField field, MissionValue value) {
        super(field, value);
    }

    @Override
    public void appendView(Context context, RelativeLayout layout) {
        TextView textView = new TextView(context);
        setRelativeLayoutParams(textView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 52, 0, 0, 0);
        textView.setMinHeight(dpToPx(context, 40));
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setHint(mField.getLabel());
        textView.setText(mField.getValue());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(Color.BLACK);

        updateValue();

        layout.addView(textView);
    }

    @Override
    public void updateValue() {
        mValue.setValue("");
    }

    @Override
    public void updateView() {

    }

    @Override
    public void updateEnabled(boolean enabled) {

    }
}
