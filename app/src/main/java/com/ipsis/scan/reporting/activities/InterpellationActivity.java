package com.ipsis.scan.reporting.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.data.CacheManager;

public class InterpellationActivity extends ReportingActivity {

    public final static String EXTRA_REPORT_INDEX = "report_index";

    public final static String EXTRA_INTERPELLATION_INDEX = "interpellation_index";

    private int mReportIndex;

    private int mInterpellationIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("activitest", "activité " + this.getLocalClassName()  );


        mReportIndex = getIntent().getIntExtra(EXTRA_REPORT_INDEX, -1);
        mInterpellationIndex = getIntent().getIntExtra(EXTRA_INTERPELLATION_INDEX, -1);

        setContentView(R.layout.activity_interpellation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public int getReportIndex() {
        return mReportIndex;
    }

    public int getInterpellationIndex() {
        return mInterpellationIndex;
    }

    public void setInterpellationIndex(int interpellationIndex) {
        mInterpellationIndex = interpellationIndex;
    }
}
