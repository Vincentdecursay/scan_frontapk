package com.ipsis.scan.reporting.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.utils.Constants;

public class CreateActivity extends ReportingActivity {

    /**
     * Mission type extra
     * See Constants
     */
    public final static String EXTRA_FORM_TYPE = "type";

    public final static String EXTRA_FORM_DATE_TIME_START = "date_time_start";

    public final static String EXTRA_FORM_DATE_TIME_END = "date_time_end";

    public final static String EXTRA_FORM_LAST_REPORT = "last_report";

    /**
     * Current type
     */
    protected int mType;

    private String mDateTimeStart;

    private String mDateTimeEnd;

    private MissionReport mLastReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mType = getIntent().getIntExtra(EXTRA_FORM_TYPE, Constants.TYPE_HORS_RESEAU);
        mDateTimeStart = getIntent().getStringExtra(EXTRA_FORM_DATE_TIME_START);
        if (mDateTimeStart == null) {
            mDateTimeStart = "";
        }
        mDateTimeEnd = getIntent().getStringExtra(EXTRA_FORM_DATE_TIME_END);
        if (mDateTimeEnd == null) {
            mDateTimeEnd = "";
        }
        Object lastReport = getIntent().getSerializableExtra(EXTRA_FORM_LAST_REPORT);
        if (lastReport != null && lastReport instanceof MissionReport) {
            mLastReport = (MissionReport) lastReport;
        }

        setContentView(R.layout.activity_create);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getString(R.string.create_title) + " (";
        switch (mType) {
            case Constants.TYPE_SUR_LIGNE:
                title += getString(R.string.create_sur_ligne);
                break;
            case Constants.TYPE_LIEU_FIXE:
                title += getString(R.string.create_lieu_fixe);
                break;
            case Constants.TYPE_HORS_RESEAU:
                title += getString(R.string.create_hors_reseau);
                break;
        }
        title += ")";
        setTitle(title);
    }

    @Override
    public void onBackPressed() {
        askCloseActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                askCloseActivity();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void askCloseActivity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.create_back_message)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    public int getType() {
        return mType;
    }

    public String getDateTimeStart() {
        return mDateTimeStart;
    }

    public String getDateTimeEnd() {
        return mDateTimeEnd;
    }

    public MissionReport getLastReport() {
        return mLastReport;
    }
}
