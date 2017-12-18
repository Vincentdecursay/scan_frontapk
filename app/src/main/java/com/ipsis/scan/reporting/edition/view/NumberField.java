package com.ipsis.scan.reporting.edition.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionValue;

import static com.ipsis.scan.utils.AppUtils.*;

/**
 * Created by pobouteau on 9/30/16.
 */

public class NumberField extends ViewField {

    private EditText mEditText;
    private ImageView mPlusImageView;
    private ImageView mMinusImageView;

    public NumberField(MissionField field, MissionValue value) {
        super(field, value);
    }

    @Override
    public void appendView(final Context context, RelativeLayout layout) {
        LinearLayout inputLayout = new LinearLayout(context);
        setRelativeLayoutParams(inputLayout, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 52, 0, 0, 0);
        inputLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(context);
        setLinearLayoutParams(textView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 0, 0, 0);
        textView.setPadding(0, 0, dpToPx(context, 16), 0);
        textView.setText(mField.getLabel());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setTextColor(Color.parseColor("#9e9e9e"));

        LinearLayout linearLayout = new LinearLayout(context);

        mEditText = new AppCompatEditText(context);
        setLinearLayoutParams(mEditText, context, 0, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
        setLinearLayoutWeight(mEditText, 0.65f);
        mEditText.setGravity(Gravity.CENTER_VERTICAL);
        mEditText.setHintTextColor(Color.BLACK);
        mEditText.setTextColor(Color.BLACK);
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.setBackgroundColor(Color.TRANSPARENT);
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateValue();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mPlusImageView = new ImageView(context);
        setLinearLayoutParams(mPlusImageView, context, 0, dpToPx(context, 26), 0, 0, 0, 0);
        setLinearLayoutWeight(mPlusImageView, 0.15f);
        mPlusImageView.setImageResource(R.drawable.ic_add_black_36dp);
        mPlusImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mPlusImageView.setBackgroundResource(getSelectableBackgroundId(context));
        mPlusImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = 0;

                try {
                    value = Integer.parseInt(mValue.getValue()) + 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mEditText.setText(String.valueOf(value));
            }
        });

        mMinusImageView = new ImageView(context);
        setLinearLayoutParams(mMinusImageView, context, 0, dpToPx(context, 26), 0, 0, 0, 0);
        setLinearLayoutWeight(mMinusImageView, 0.2f);
        mMinusImageView.setImageResource(R.drawable.ic_remove_black_36dp);
        mMinusImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mMinusImageView.setBackgroundResource(getSelectableBackgroundId(context));
        mMinusImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = 0;

                try {
                    value = Integer.parseInt(mValue.getValue()) - 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (value < 0) {
                    value = 0;
                }

                mEditText.setText(String.valueOf(value));
            }
        });

        updateView();

        linearLayout.addView(mEditText);
        linearLayout.addView(mMinusImageView);
        linearLayout.addView(mPlusImageView);

        inputLayout.addView(textView);
        inputLayout.addView(linearLayout);

        layout.addView(inputLayout);
    }

    @Override
    public void updateValue() {
        mValue.setValue(mEditText.getText().toString());
    }

    @Override
    public void updateView() {
        mEditText.setText(mValue.getValue());
    }

    @Override
    public void updateEnabled(boolean enabled) {
        mEditText.setEnabled(enabled);
        mPlusImageView.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        mMinusImageView.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }
}
