package com.ipsis.scan.reporting.activities.search;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.ipsis.scan.R;
import com.ipsis.scan.database.PlaceDataSource;
import com.ipsis.scan.database.RatpDataSource;
import com.ipsis.scan.database.model.Route;
import com.ipsis.scan.database.model.Stop;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.model.MissionLocation;
import com.ipsis.scan.security.locking.LockManager;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by etude on 23/10/15.
 */
public class SearchLocationActivity extends AppCompatActivity {

    public final static String EXTRA_SEARCH_TYPE = "type";
    public final static String EXTRA_SEARCH_LOCATION = "location";
    public final static String EXTRA_SEARCH_ROUTE = "route";
    public final static String EXTRA_SEARCH_STOP = "stop";

    private static SearchLocationCallback sCallback;

    private int mType;

    private MissionLocation mLocation;

    private RatpDataSource mRatpDataSource;

    private ListView mResultListView;
    private final ArrayList<SearchResult> mSearchResults;
    private SearchResultAdapter mSearchResultAdapter;

    private ListView mStationsListView;
    private final ArrayList<SearchResult> mSearchStationsResults;
    private SearchResultAdapter mSearchStationsResultAdapter;

    private EditText mSearchEditText;
    private TextView mLocationTextView;

    public SearchLocationActivity() {
        super();

        mSearchResults = new ArrayList<>();

        mSearchStationsResults = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.i("activitest", "activité SearchLocationActivity ");

        mType = getIntent().getIntExtra(EXTRA_SEARCH_TYPE, Constants.TYPE_SEARCH_ROUTE);

        mLocation = (MissionLocation) getIntent().getSerializableExtra(EXTRA_SEARCH_LOCATION);
        if (mLocation == null) {
            mLocation = new MissionLocation();
        }

        mRatpDataSource = new RatpDataSource(this);

        mResultListView = (ListView) findViewById(R.id.resultListView);
        mSearchResultAdapter = new SearchResultAdapter(this, mSearchResults);

        mLocationTextView = (TextView) findViewById(R.id.locationTextView);
        mStationsListView = (ListView) findViewById(R.id.stationsListView);
        mSearchStationsResultAdapter = new SearchResultAdapter(this, mSearchStationsResults);

        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
        mSearchEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        // List
        mSearchResultAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                updateSearchResults();
            }
        });
        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchResult searchResult = mSearchResults.get(i);

                onClick(searchResult);
            }
        });
        mResultListView.setAdapter(mSearchResultAdapter);
        AppUtils.justifyListViewHeightBasedOnChildren(mResultListView);

        mSearchStationsResultAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                AppUtils.justifyListViewHeightBasedOnChildren(mStationsListView);
            }
        });
        mStationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SearchResult searchResult = mSearchStationsResults.get(i);

                onClick(searchResult);
            }
        });
        mStationsListView.setAdapter(mSearchStationsResultAdapter);
        AppUtils.justifyListViewHeightBasedOnChildren(mStationsListView);

        mSearchEditText.setText(mLocation.getLocation());
        mSearchEditText.setSelection(mSearchEditText.getText().length());
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });

        ImageView backButton = (ImageView) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopActivity();
            }
        });

        ImageView clearButton = (ImageView) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchEditText.getText().toString().isEmpty()) {
                    stopActivity();
                } else {
                    mSearchEditText.setText("");
                }
            }
        });

        View closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopActivity();
            }
        });

        updateSearchResults();
        search(mSearchEditText.getText().toString());

    }

    private void updateSearchResults() {
        TextView historyTextView = (TextView) findViewById(R.id.historyTextView);
        View noResultLayout = findViewById(R.id.noResultLayout);

        if (mSearchResults.size() == 0 && mSearchEditText.getText().length() == 0) {
            // Set history
            for (SearchLocationHistory history : CacheManager.getInstance().getData().getLocationHistory()) {
                if (history.getType() == mType) {
                    SearchResult searchResult = new SearchResult(history.getTitle());
                    searchResult.setTag(history.getTag());

                    if (history.getType() == Constants.TYPE_SEARCH_ROUTE) {
                        Route route = (Route) history.getTag();
                        searchResult.setIcon(route.getShortName());
                    }

                    mSearchResults.add(searchResult);

                    if (mSearchResults.size() >= 3) {
                        break;
                    }
                }
            }

            if (mSearchResults.size() > 0) {
                mSearchResultAdapter.notifyDataSetChanged();
                historyTextView.setVisibility(View.VISIBLE);
            } else {
                historyTextView.setVisibility(View.GONE);
            }

            if (mSearchStationsResults.size() > 0) {
                mLocationTextView.setVisibility(View.VISIBLE);
                mStationsListView.setVisibility(View.VISIBLE);
            } else {
                mLocationTextView.setVisibility(View.GONE);
                mStationsListView.setVisibility(View.GONE);
            }
        } else {
            historyTextView.setVisibility(View.GONE);
            mLocationTextView.setVisibility(View.GONE);
            mStationsListView.setVisibility(View.GONE);
        }

        if (mSearchResults.size() > 0) {
            noResultLayout.setVisibility(View.GONE);
        } else {
            noResultLayout.setVisibility(View.VISIBLE);
        }

        AppUtils.justifyListViewHeightBasedOnChildren(mResultListView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.transition_fade_in, R.anim.transition_fade_out);

        setResult(RESULT_CANCELED);
    }

    private void onClick(SearchResult searchResult) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SEARCH_TYPE, mType);
        intent.putExtra(EXTRA_SEARCH_LOCATION, new MissionLocation(searchResult.getTitle(), searchResult.getIcon()));

        switch (mType) {
            case Constants.TYPE_SEARCH_ROUTE:
                intent.putExtra(EXTRA_SEARCH_ROUTE, (Route) searchResult.getTag());
                break;
            case Constants.TYPE_SEARCH_STOP:
                intent.putExtra(EXTRA_SEARCH_STOP, (Stop) searchResult.getTag());
                break;
        }

        setResult(RESULT_OK, intent);

        if (sCallback != null) {
            sCallback.onLocationSelected(intent);
            sCallback = null;
        }

        CacheManager.getInstance().getData().addLocationHistory(new SearchLocationHistory(mType, new MissionLocation(searchResult.getTitle(), searchResult.getIcon()), searchResult.getTag()));
        CacheManager.getInstance().saveCache(SearchLocationActivity.this);

        finish();
    }

    public static void startActivity(Context context, Intent intent, SearchLocationCallback callback) {
        sCallback = callback;

        context.startActivity(intent);
    }

    public void stopActivity() {
        setResult(RESULT_CANCELED);

        finish();

        overridePendingTransition(R.anim.transition_fade_in, R.anim.transition_fade_out);
    }

    public void search(String search) {
        switch (mType) {
            case Constants.TYPE_SEARCH_ROUTE:
                SearchRouteTask searchRouteTask = new SearchRouteTask(search);
                searchRouteTask.start();
                break;
            case Constants.TYPE_SEARCH_STOP:
                SearchStopTask searchStopTask = new SearchStopTask(search);
                searchStopTask.start();
                break;
            case Constants.TYPE_SEARCH_LOCATION:
                if (search.length() > 0) {
                    if (mSearchResults.size() > 0) {
                        mSearchResults.set(0, new SearchResult(new MissionLocation(mSearchEditText.getText().toString())));
                    } else {
                        mSearchResults.add(0, new SearchResult(new MissionLocation(mSearchEditText.getText().toString())));
                    }
                    mSearchResultAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

        LockManager.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LockManager.getInstance().onPause(this);
    }

    public interface SearchLocationCallback {
        void onLocationSelected(Intent intent);
    }

    private class SearchRouteTask extends Thread {

        private String mSearch;

        private SearchRouteTask(String search) {
            super();

            mSearch = search;
        }

        @Override
        public void run() {
            List<Route> routes;
            synchronized (mRatpDataSource) {
                routes = mRatpDataSource.searchRoute(mSearch);
            }

            /*Collections.sort(routes, new Comparator<Route>() {
                @Override
                public int compare(Route r1, Route r2) {
                    int lineNumber1 = r1.getLineNumber();
                    int lineNumber2 = r2.getLineNumber();

                    if (lineNumber1 != -1 && lineNumber2 != -1) {
                        if (lineNumber1 <= lineNumber2) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (lineNumber1 == -1) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
            });*/

            synchronized (mSearchResults) {
                mSearchResults.clear();

                for (Route route : routes) {
                    SearchResult searchResult = new SearchResult(new MissionLocation(route.getLongName(), route.getShortName()));
                    searchResult.setTag(route);
                    mSearchResults.add(searchResult);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSearchResultAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private class SearchStopTask extends Thread {

        private String mSearch;

        private SearchStopTask(String search) {
            super();

            mSearch = search;
        }

        @Override
        public void run() {
            List<Stop> stops;
            synchronized (mRatpDataSource) {
                stops = mRatpDataSource.searchStop(mSearch);
            }

            List<Stop> filteredStops = new ArrayList<>();

            for (Stop stop : stops) {
                boolean found = false;

                for (Stop filteredStop : filteredStops) {
                    if (stop.getName().equals(filteredStop.getName())) {
                        found = true;

                        break;
                    }
                }

                if (!found) {
                    filteredStops.add(stop);
                }
            }

            synchronized (mSearchResults) {
                mSearchResults.clear();

                for (Stop stop : filteredStops) {
                    SearchResult searchResult = new SearchResult(new MissionLocation(stop.getName()));
                    searchResult.setTag(stop);
                    mSearchResults.add(searchResult);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSearchResultAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }
}
