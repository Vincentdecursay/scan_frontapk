package com.ipsis.scan.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.ipsis.scan.reporting.activities.search.SearchLocationActivity;
import com.ipsis.scan.utils.Constants;

/**
 * Created by pobouteau on 9/26/16.
 */

public class LocationPicker extends TextView {

    private int mSearchLocationType;
    private OnLocationChangedListener mOnLocationChangedListener;

    public LocationPicker(Context context) {
        super(context);

        init();
    }

    public LocationPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public LocationPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @TargetApi(21)
    public LocationPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    public void init() {
        mSearchLocationType = Constants.TYPE_SEARCH_ROUTE;
    }

    public void clear() {
        setText("");

        startSearchLocationActivity();
    }

    public void startSearchLocationActivity() {
        Intent intent = new Intent(getContext(), SearchLocationActivity.class);
        intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_TYPE, mSearchLocationType);
        intent.putExtra(SearchLocationActivity.EXTRA_SEARCH_LOCATION, getText().toString()); // TODO update with missionlocation
        getContext().startActivity(intent);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearchLocationActivity();
            }
        });
    }

    public void setSearchLocationType(int searchLocationType) {
        mSearchLocationType = searchLocationType;
    }

    public interface OnLocationChangedListener {
        void onLocationChanged(String location);
    }
}
