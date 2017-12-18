package com.ipsis.scan.reporting.communication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by pobouteau on 10/27/16.
 */

public class SynchronisationTimeoutAlarm extends BroadcastReceiver {
    public static final String EXTRA_USERNAME = "username";

    @Override
    public void onReceive(Context context, Intent intent) {
        IBinder binder = peekService(context, new Intent(context, SynchronisationService.class));
        if (binder != null){
            SynchronisationService service = ((SynchronisationService.LocalBinder) binder).getService();
            service.sendReports(intent.getStringExtra(EXTRA_USERNAME));
        }
    }
}
