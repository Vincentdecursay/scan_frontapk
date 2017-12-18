package com.ipsis.scan.reporting.edition.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionValue;
import com.ipsis.scan.utils.StringUtils;

import java.util.ArrayList;

import static com.ipsis.scan.utils.AppUtils.dpToPx;
import static com.ipsis.scan.utils.AppUtils.setRelativeLayoutParams;
import static com.ipsis.scan.utils.AppUtils.setRelativeLayoutRule;

/**
 * Created by pobouteau on 9/30/16.
 */

public abstract class ViewField {

    protected MissionField mField;

    protected MissionValue mValue;

    protected boolean mEnabled;

    protected boolean mHideGroup;

    protected ArrayList<View> mHideViews;

    public ViewField(MissionField field, MissionValue value) {
        super();

        mField = field;
        mValue = value;

        mHideGroup = false;

        mHideViews = new ArrayList<>();
    }

    public abstract void appendView(Context context, RelativeLayout layout);

    public ImageView addActionButton(final Context context, RelativeLayout layout, int imageResource) {
        ImageView actionButton = new ImageView(context);
        setRelativeLayoutParams(actionButton, context, dpToPx(context, 40), dpToPx(context, 40), 0, 0, 0, 0);
        setRelativeLayoutRule(actionButton, RelativeLayout.ALIGN_PARENT_END);
        actionButton.setPadding(dpToPx(context, 8), 0, dpToPx(context, 8), 0);
        actionButton.setImageResource(imageResource);
        actionButton.setClickable(true);
        actionButton.setBackgroundResource(R.drawable.ripple_white_background);

        layout.addView(actionButton);

        return actionButton;
    }

    public abstract void updateValue();

    public String getValue() {
        updateValue();

        return mValue.getValue();
    }

    public abstract void updateView();

    public void setValue(String value) {
        mValue.setValue(value);

        updateView();
    }

    public abstract void updateEnabled(boolean enabled);

    public String getNoHeaderLabel() {
        String label = mField.getLabel();
        int index;

        index = StringUtils.indexAfter(label, "hour:");
        if (index != -1) {
            return label.substring(index);
        }

        index = StringUtils.indexAfter(label, "location:");
        if (index != -1) {
            return label.substring(index);
        }

        index = StringUtils.indexAfter(label, "surname:");
        if (index != -1) {
            return label.substring(index);
        }

        index = StringUtils.indexAfter(label, "firstname:");
        if (index != -1) {
            return label.substring(index);
        }

        index = StringUtils.indexAfter(label, "hide:");
        if (index != -1) {
            mHideGroup = true;
            return label.substring(index);
        }

        return label;
    }

    public String getHeader() {
        String label = mField.getLabel();
        String[] tokens = label.split(":");

        if (tokens.length >= 2) {
            return tokens[0];
        } else {
            return null;
        }
    }

    public MissionField getField() {
        return mField;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;

        updateEnabled(enabled);
    }

    public boolean isHideGroup() {
        return mHideGroup;
    }

    public ArrayList<View> getHideViews() {
        return mHideViews;
    }

    public void setHideGroupVisibility(int visibility) {
        for (View view : mHideViews) {
            view.setVisibility(visibility);
        }
    }
}
