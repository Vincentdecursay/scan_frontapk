package com.ipsis.scan.reporting.activities.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.ipsis.scan.R;
import com.ipsis.scan.security.locking.LockManager;
import com.ipsis.scan.utils.AppUtils;

import java.util.ArrayList;

/**
 * Created by etude on 23/10/15.
 */
public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.BOTTOM);

        final ListView resultListView = (ListView) findViewById(R.id.resultListView);
        final ArrayList<SearchResult> searchResults = new ArrayList<>();
        final SearchResultAdapter searchResultAdapter = new SearchResultAdapter(this, searchResults);

        searchResultAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                AppUtils.justifyListViewHeightBasedOnChildren(resultListView);
            }
        });

        resultListView.setAdapter(searchResultAdapter);
        AppUtils.justifyListViewHeightBasedOnChildren(resultListView);

        final EditText searchEditText = (EditText) findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchResults.clear();

                for (int j = 0 ; j < charSequence.length() ; j++) {
                    // searchResults.add(new SearchResult("test " + j));
                }

                searchResultAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
                if (searchEditText.getText().toString().isEmpty()) {
                    stopActivity();
                } else {
                    searchEditText.setText("");
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.transition_fade_in, R.anim.transition_fade_out);
    }

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, SearchActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.transition_fade_in, R.anim.transition_fade_out);
    }

    public void stopActivity() {
        finish();
        overridePendingTransition(R.anim.transition_fade_in, R.anim.transition_fade_out);
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
}
