package com.ipsis.scan.reporting.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.Constants;

public class EditActivity extends ReportingActivity {

    /**
     * Index of the report in cache extra
     */
    public final static String EXTRA_REPORT_INDEX = "index";

    /**
     * Current report index in cache
     */
    private int mReportIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("activitest", "activité " + this.getLocalClassName()  );


        mReportIndex = getIntent().getIntExtra(EXTRA_REPORT_INDEX, -1);

        setContentView(R.layout.activity_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);

        /*MissionReport missionReport = CacheManager.getInstance().getData().getMissionReports().get(mReportIndex);
        if (!missionReport.getMissionType().getForm().getInterpellationEnable().equals(Constants.INTERPELLATION_ENABLED)) {
            MenuItem menuItem = menu.findItem(R.id.action_add_interpellation);
            if (menuItem != null) {
                menuItem.setVisible(false);
            }
        }*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MissionReport missionReport = CacheManager.getInstance().getData().getMissionReports().get(mReportIndex);

        switch (item.getItemId()) {
            /*case R.id.action_add_interpellation:
                if (!missionReport.isSync() && !missionReport.isSyncing()) {
                    Intent intent = new Intent(EditActivity.this, InterpellationActivity.class);
                    intent.putExtra(InterpellationActivity.EXTRA_REPORT_INDEX, mReportIndex);
                    intent.putExtra(InterpellationActivity.EXTRA_INTERPELLATION_INDEX, -1);
                    startActivity(intent);
                } else {
                    AppUtils.showErrorDialog(this, R.string.edit_report_sent_interpellation);
                }

                return true;*/
            case R.id.action_remove_report:
                if (!missionReport.isSync() && !missionReport.isSyncing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                    builder.setMessage(R.string.edit_dialog_remove_report)
                            .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CacheManager.getInstance().getData().getMissionReports().remove(mReportIndex);
                                    CacheManager.getInstance().saveCache(EditActivity.this);

                                    finish();
                                }
                            }).setNeutralButton(R.string.button_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.create().show();
                } else {
                    AppUtils.showErrorDialog(this, R.string.edit_report_sent_remove);
                }

                return true;
            case android.R.id.home:
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public int getReportIndex() {
        return mReportIndex;
    }
}
