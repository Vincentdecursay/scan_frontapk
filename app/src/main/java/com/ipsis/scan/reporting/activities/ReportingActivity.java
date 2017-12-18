package com.ipsis.scan.reporting.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.security.locking.LockManager;
import com.ipsis.scan.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pobouteau on 9/22/16.
 */
public class ReportingActivity extends AppCompatActivity implements CacheManager.ConnectionStateListener {

    /**
     * Time updateDateTime broadcast receiver
     */
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onStart() {
        super.onStart();

        final TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        final TextView timeTextView = (TextView) findViewById(R.id.timeTextView);

        updateDateTime(dateTextView, timeTextView);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    updateDateTime(dateTextView, timeTextView);
                }
            }
        };
        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        setConnectionStateMode(!CacheManager.getInstance().isOfflineMode());
        setReportDate();

        CacheManager.getInstance().registerConnectionStateListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View current = getCurrentFocus();
        if (current != null) {
            current.clearFocus();
        }

        LockManager.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LockManager.getInstance().onPause(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        CacheManager.getInstance().unregisterConnectionStateListener(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            LockManager.getInstance().reset();
        }

        return super.dispatchTouchEvent(motionEvent);
    }

    /**
     * Update textviews
     * @param dateTextView date TextView
     * @param timeTextView time TextView
     */
    public void updateDateTime(TextView dateTextView, TextView timeTextView) {
        Date date = new Date();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dateTextView.setText(dateFormatter.format(date));
        timeTextView.setText(timeFormatter.format(date));
    }

    /**
     * Show offline state layout
     * @param online
     */
    public void setConnectionStateMode(boolean online) {
        RelativeLayout connectionStateLayout = (RelativeLayout) findViewById(R.id.connectionStateLayout);
        TextView connectionStateTextView = (TextView) findViewById(R.id.connectionStateTextView);
        if (connectionStateTextView != null && connectionStateLayout != null) {
            if (online) {
                connectionStateTextView.setVisibility(View.GONE);
            } else {
                connectionStateTextView.setVisibility(View.VISIBLE);
            }

            connectionStateLayout.invalidate();
        }
    }

    public void setReportDate() {
        TextView reportDateTextView = (TextView) findViewById(R.id.reportDateTextView);

        if (reportDateTextView != null) {
            Calendar date = Calendar.getInstance();
            Calendar cacheDate = CacheManager.getInstance().getData().getDate();
            if (!DateUtils.isSameDay(cacheDate, date)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateFormatted = dateFormat.format(cacheDate.getTime());

                reportDateTextView.setText(getString(R.string.reporting_date, dateFormatted));
                reportDateTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onConnectedStateChanged(final boolean online) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setConnectionStateMode(online);

                CacheManager.getInstance().saveCache(ReportingActivity.this);
            }
        });
    }
}
