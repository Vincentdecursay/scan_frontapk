package com.ipsis.scan.reporting.edition.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.activities.search.SearchLocationActivity;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionLocation;
import com.ipsis.scan.reporting.edition.model.MissionValue;
import com.ipsis.scan.utils.Constants;

import static com.ipsis.scan.utils.AppUtils.*;

/**
 * Created by pobouteau on 9/30/16.
 */

public class PlaceField extends ViewField implements SearchLocationActivity.SearchLocationCallback {

    private TextView mTextView;

    private int mSearchType;
    private ImageView mActionButton;
    private View.OnClickListener mOnClickListener;

    public PlaceField(MissionField field, MissionValue value) {
        super(field, value);

        mSearchType = Constants.TYPE_SEARCH_LOCATION;
    }

    @Override
    public void appendView(final Context context, RelativeLayout layout) {
        mActionButton = addActionButton(context, layout, R.drawable.ic_clear_black_36dp);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextView.setText("");

                updateValue();

                startSearchLocationActivity(context);
            }
        });
        setRelativeLayoutRule(mActionButton, RelativeLayout.ALIGN_PARENT_BOTTOM);

        LinearLayout inputLayout = new LinearLayout(context);
        setRelativeLayoutParams(inputLayout, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 52, 0, 44, 0);
        inputLayout.setOrientation(LinearLayout.VERTICAL);

        TextView labelTextView = new TextView(context);
        setLinearLayoutParams(labelTextView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 0, 0, 0);
        labelTextView.setText(getNoHeaderLabel());
        labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        labelTextView.setTextColor(Color.parseColor("#9e9e9e"));

        mTextView = new TextView(context);
        setLinearLayoutParams(mTextView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 7, 0, 6);
        //setRelativeLayoutParams(mTextView, context, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 52, 0, 44, 0);
        //setRelativeLayoutRule(mTextView, RelativeLayout.CENTER_VERTICAL);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        // mTextView.setHint(getNoHeaderLabel());
        mTextView.setHint(R.string.field_select_hint);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mTextView.setTextColor(Color.BLACK);

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchLocationActivity(context);
            }
        };
        mTextView.setOnClickListener(mOnClickListener);

        updateView();

        inputLayout.addView(labelTextView);
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

    public void startSearchLocationActivity(Context context) {
        Intent intent = new Intent(context, SearchLocationActivity.class);
        intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, mSearchType);
        intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_LOCATION, new MissionLocation(mTextView.getText().toString())); // TODO setTag pour mSearchType

        SearchLocationActivity.startActivity(context, intent, this);
    }

    @Override
    public void onLocationSelected(Intent intent) {
        MissionLocation location = (MissionLocation) intent.getSerializableExtra(SearchLocationActivity.EXTRA_SEARCH_LOCATION);
        if (location != null) {
            mTextView.setText(location.toString());

            updateValue();
        }
    }

    public int getSearchType() {
        return mSearchType;
    }

    public void setSearchType(int searchType) {
        mSearchType = searchType;
    }
}
