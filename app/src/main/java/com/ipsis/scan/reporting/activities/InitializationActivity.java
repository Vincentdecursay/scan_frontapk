package com.ipsis.scan.reporting.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import com.ipsis.scan.R;

public class InitializationActivity extends ReportingActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_initialization);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.initialization_title);
    }
}
