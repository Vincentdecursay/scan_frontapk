package com.ipsis.scan.reporting.edition.view;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionValue;

import static com.ipsis.scan.utils.AppUtils.setLinearLayoutParams;
import static com.ipsis.scan.utils.AppUtils.setRelativeLayoutParams;

/**
 * Created by pobouteau on 9/30/16.
 */

public class EditTextField extends ViewField {

    private TextInputEditText mEditText;

    // private ImageView mActionButton;

    public EditTextField(MissionField field, MissionValue value) {
        super(field, value);
    }

    @Override
    public void appendView(final Context context, RelativeLayout layout) {
        TextInputLayout inputLayout = new TextInputLayout(context);
        setRelativeLayoutParams(inputLayout, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 50, 0, 44, 0);

        mEditText = new TextInputEditText(context);
        setLinearLayoutParams(mEditText, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, 0, 0, 0);
        mEditText.setHint(getNoHeaderLabel());
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mEditText.setTextColor(Color.BLACK);
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

        /*mActionButton = addActionButton(context, layout, R.drawable.ic_clear_black_36dp);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditText.setText("");

                updateValue();
            }
        });*/

        updateView();

        inputLayout.addView(mEditText);
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
        // mActionButton.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }
}
