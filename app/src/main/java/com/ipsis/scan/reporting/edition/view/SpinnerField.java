package com.ipsis.scan.reporting.edition.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionValue;
import com.ipsis.scan.utils.JSONUtils;
import org.json.JSONException;

import java.util.ArrayList;

import static com.ipsis.scan.utils.AppUtils.*;

/**
 * Created by pobouteau on 9/30/16.
 */

public class SpinnerField extends ViewField {

    private TextView mTextView;
    private View.OnClickListener mOnClickListener;
    private ImageView mActionButton;
    private CharSequence[] mItems;

    public SpinnerField(MissionField field, MissionValue value) {
        super(field, value);

        try {
            ArrayList<String> array = JSONUtils.JSONArrayToStringArray(mField.getValue());
            mItems = new CharSequence[array.size()];

            for (int i = 0; i < array.size(); i++) {
                mItems[i] = array.get(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (value.getValue().equals(field.getValue())) {
            if (mItems.length > 0) {
                value.setValue(mItems[0].toString());
            } else {
                value.setValue("");
            }
        }
    }

    @Override
    public void appendView(final Context context, RelativeLayout layout) {
        mActionButton = addActionButton(context, layout, R.drawable.ic_arrow_drop_down_black_36dp);
        mActionButton.setPadding(dpToPx(context, 4), 0, dpToPx(context, 4), 0);
        setRelativeLayoutRule(mActionButton, RelativeLayout.ALIGN_PARENT_BOTTOM);

        LinearLayout inputLayout = new LinearLayout(context);
        setRelativeLayoutParams(inputLayout, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 52, 0, 0, 0);
        inputLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(context);
        setLinearLayoutParams(textView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 0, 0, 0);
        textView.setText(mField.getLabel());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setTextColor(Color.parseColor("#9e9e9e"));

        mTextView = new TextView(context);
        setLinearLayoutParams(mTextView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 7, 0, 6);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setHintTextColor(Color.BLACK);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mTextView.setTextColor(Color.BLACK);

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] finalItems = mItems;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(mField.getLabel());
                builder.setItems(mItems, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mTextView.setText(finalItems[item]);

                        updateValue();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        };
        mTextView.setOnClickListener(mOnClickListener);

        if (mField.getValue().equals(mValue.getValue())) {
            mTextView.setText("");
        } else {
            updateView();
        }

        inputLayout.addView(textView);
        inputLayout.addView(mTextView);
        layout.addView(inputLayout);
    }

    @Override
    public void updateValue() {
        mValue.setValue(mTextView.getText().toString());
    }

    @Override
    public void updateView() {
        mTextView.setText(mValue.getValue());
    }

    @Override
    public void updateEnabled(boolean enabled) {
        if (enabled) {
            mTextView.setOnClickListener(mOnClickListener);
        } else {
            mTextView.setOnClickListener(null);
        }
        mActionButton.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }
}
