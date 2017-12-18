package com.ipsis.scan.reporting.communication.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.ipsis.scan.R;
import com.ipsis.scan.communication.sending.NetworkConfiguration;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.reporting.activities.SummaryActivity;
import com.ipsis.scan.reporting.data.CacheData;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.model.MissionInterpellation;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.utils.DateUtils;
import com.ipsis.scan.utils.SerializationUtils;
import com.stanfy.enroscar.goro.Goro;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by pobouteau on 10/25/16.
 */

public class SynchronisationService extends Service {

    public static final String USER_SYNCED_PREFERENCES = "UserSyncedPreferences";

    private static final String USERS_LIST_PREFERENCE = "usersList";

    /**
     * Service binder
     */
    private final LocalBinder mLocalBinder;

    /**
     * Task scheduler
     */
    private final Goro mGoro;

    /**
     * Semaphore
     */
    private final Object mTaskLock;

    /**
     * Current tasks number
     */
    private final HashMap<String, Integer> mTaskNumber;

    private final HashMap<String, UserData> mData;

    private final HashMap<String, MissionReport> mRetryReports;

    private final HashMap<String, MissionInterpellation> mRetryInterpellations;

    public SynchronisationService() {
        super();

        mLocalBinder = new LocalBinder();

        mGoro = Goro.create();

        mTaskLock = new Object();
        mTaskNumber = new HashMap<>();

        mData = new HashMap<>();
        mRetryReports = new HashMap<>();
        mRetryInterpellations = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent nIntent = new Intent(this, SummaryActivity.class);
        // nIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setTicker(getString(R.string.app_name))
                .setContentText(getString(R.string.service_uploading))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    public CacheData getData() {
        UserData userData = mData.get(NetworkManager.getInstance().getUsername());
        if (userData != null) {
            return userData.getCacheData();
        } else {
            return null;
        }
    }

    public void updateServiceData(CacheData cacheData) {
        UserData userData = mData.get(NetworkManager.getInstance().getUsername());
        if (userData != null && userData.getCacheData() != null) {
            userData.setCacheData(cacheData);

            for (MissionReport report : userData.getCacheData().getMissionReports()) {
                report.setCopy(true);
                report.setSyncByUser(false);

                for (MissionInterpellation interpellation : report.getInterpellations()) {
                    interpellation.setCopy(true);
                    interpellation.setSyncByUser(false);
                }
            }
        }

        checkServiceNeeded();
    }

    public void sendReports(final String username) {
        UserData userData = mData.get(username);

        if (userData != null) {
            if (userData.getCacheData() != null) {
                sendReports(userData.getCacheData().getNetworkConfiguration(), userData.getCacheData().getMissionReports());
            } else {
                new RetrySendReportsTask(username).start();
            }
        }
    }

    private class RetrySendReportsTask extends Thread {

        private String mUsername;

        public RetrySendReportsTask(String username) {
            super();

            mUsername = username;
        }

        @Override
        public void run() {
            try {
                sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sendReports(mUsername);
        }
    }

    public void sendReports(final NetworkConfiguration configuration, final ArrayList<MissionReport> missionReports) {
        final NetworkConfiguration networkConfiguration = configuration.copy();

        cancelSynchronisation();

        for (MissionReport missionReport : missionReports) {
            sendReport(networkConfiguration, missionReport);
        }
    }

    public void sendReport(final NetworkConfiguration networkConfiguration, final MissionReport missionReport) {
        synchronized (mTaskLock) {
            // mTaskNumber++;
            Integer number = mTaskNumber.get(networkConfiguration.getUsername());
            if (number == null) {
                number = 0;
            }
            number++;
            mTaskNumber.put(networkConfiguration.getUsername(), number);
        }

        synchronized (mRetryReports) {
            mRetryReports.put(missionReport.getLocalId(), missionReport);
        }

        mGoro.schedule(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    missionReport.send(getApplicationContext(), networkConfiguration, new MissionReport.SyncCallback() {
                        @Override
                        public void onSyncSucceed() {
                            Log.e("report", "onSyncSucceed");

                            synchronized (mRetryReports) {
                                if (mRetryReports.containsKey(missionReport.getLocalId())) {
                                    mRetryReports.remove(missionReport.getLocalId());
                                }
                            }

                            checkExistingTasks(networkConfiguration.getUsername());
                        }

                        @Override
                        public void onSyncFailure(Exception e) {
                            Log.e("report", "onSyncFailure", e);

                            new RetrySendReportTask(networkConfiguration.getUsername(), missionReport).start();

                            //checkExistingTasks();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();

                    new RetrySendReportTask(networkConfiguration.getUsername(), missionReport).start();

                    //checkExistingTasks();
                }

                return null;
            }
        });
    }

    private class RetrySendReportTask extends Thread {

        private String mUsername;

        private MissionReport mMissionReport;

        public RetrySendReportTask(String username, MissionReport missionReport) {
            super();

            mUsername = username;
            mMissionReport = missionReport;
        }

        @Override
        public void run() {
            try {
                sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            NetworkConfiguration configuration = getNetworkConfiguration(mUsername);
            if (configuration != null) {
                synchronized (mTaskLock) {
                    Integer number = mTaskNumber.get(configuration.getUsername());
                    if (number == null) {
                        number = 1;
                    }
                    number--;
                    mTaskNumber.put(configuration.getUsername(), number);
                }

                sendReport(configuration, mMissionReport);
            }
        }
    }

    public void sendInterpellation(final NetworkConfiguration configuration, final MissionInterpellation missionInterpellation) {
        final NetworkConfiguration networkConfiguration = configuration.copy();

        /*synchronized (mTaskLock) {
            mTaskNumber++;
        }*/

        synchronized (mRetryInterpellations) {
            mRetryInterpellations.put(missionInterpellation.getLocalId(), missionInterpellation);
        }

        mGoro.schedule(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    missionInterpellation.send(getApplicationContext(), networkConfiguration, new MissionReport.SyncCallback() {
                        @Override
                        public void onSyncSucceed() {
                            Log.e("inter", "onSyncSucceed");

                            synchronized (mRetryInterpellations) {
                                if (mRetryInterpellations.containsKey(missionInterpellation.getLocalId())) {
                                    mRetryInterpellations.remove(missionInterpellation.getLocalId());
                                }
                            }

                            //checkExistingTasks();
                        }

                        @Override
                        public void onSyncFailure(Exception e) {
                            Log.e("inter", "onSyncFailure", e);

                            new RetrySendInterpellationTask(networkConfiguration.getUsername(), missionInterpellation).start();

                            //checkExistingTasks();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();

                    new RetrySendInterpellationTask(networkConfiguration.getUsername(), missionInterpellation).start();

                    //checkExistingTasks();
                }

                return null;
            }
        });
    }

    private class RetrySendInterpellationTask extends Thread {

        private String mUsername;

        private MissionInterpellation mMissionInterpellation;

        public RetrySendInterpellationTask(String username, MissionInterpellation missionInterpellation) {
            super();

            mUsername = username;
            mMissionInterpellation = missionInterpellation;
        }

        @Override
        public void run() {
            try {
                sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            NetworkConfiguration configuration = getNetworkConfiguration(mUsername);
            if (configuration != null) {
                sendInterpellation(configuration, mMissionInterpellation);
            }
        }
    }

    public void scheduleSynchronisation() { // TODO show warning
        String username = NetworkManager.getInstance().getUsername();
        UserData userData = mData.get(username);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, SynchronisationTimeoutAlarm.class);
        intent.putExtra(SynchronisationTimeoutAlarm.EXTRA_USERNAME, username);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (userData == null) {
            userData = new UserData();
            mData.put(username, userData);
            userData.setUsername(username);
        } else {
            alarmManager.cancel(userData.getAlarmIntent());
        }

        setUserSynced(username, CacheManager.getInstance().getData().isSynced());

        updateServiceData(SerializationUtils.deepClone(CacheManager.getInstance().getData()));
        userData.setAlarmIntent(pendingIntent);

        long time = CacheManager.getInstance().getData().getEndDate().getTimeInMillis() + 10 * 60 * 1000;
        if (time < System.currentTimeMillis()) {
            time = System.currentTimeMillis() + 1000 * 60 * 60;
        } // TODO remove comments
        //long time = System.currentTimeMillis() + 1000 * 60 * 2;
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void setUserSynced(String username, boolean synced) {
        SharedPreferences preferences = getSharedPreferences(USER_SYNCED_PREFERENCES, Context.MODE_PRIVATE);
        String usersList = preferences.getString(USERS_LIST_PREFERENCE, "{}");
        try {
            JSONObject json = new JSONObject(usersList);

            Calendar cacheDate = CacheManager.getInstance().getData().getDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateFormatted = dateFormat.format(cacheDate.getTime());

            JSONObject user = new JSONObject();
            user.put("synced", synced);
            if (!synced) {
                user.put("date", dateFormatted);
            }

            json.put(username, user);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(USERS_LIST_PREFERENCE, json.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getUsersSynced(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(USER_SYNCED_PREFERENCES, Context.MODE_PRIVATE);
        String usersList = preferences.getString(USERS_LIST_PREFERENCE, "{}");
        try {
            return new JSONObject(usersList);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public void cancelSynchronisation() {
        String username = NetworkManager.getInstance().getUsername();
        UserData userData = mData.get(username);

        if (userData != null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(userData.getAlarmIntent());
        }
    }

    public void updateLocalData(final Context context) {
        final CacheData cacheData = CacheManager.getInstance().getData();
        final UserData userData = mData.get(NetworkManager.getInstance().getUsername());
        if (cacheData != null && userData != null) {
            final CacheData serviceData = userData.getCacheData();
            if (serviceData != null) {
                for (int i = 0; i < cacheData.getMissionReports().size(); i++) {
                    MissionReport report = cacheData.getMissionReports().get(i);
                    MissionReport serviceReport = serviceData.getMissionReports().get(i);

                    synchronized (mRetryReports) {
                        if (mRetryReports.containsKey(report.getLocalId())) {
                            report = mRetryReports.get(report.getLocalId());
                            cacheData.getMissionReports().set(i, report);
                        } else {
                            report.setSync(serviceReport.isSync() || serviceReport.isSyncing());
                        }
                    }

                    ArrayList<MissionInterpellation> interpellations = report.getInterpellations();
                    ArrayList<MissionInterpellation> serviceInterpellations = serviceReport.getInterpellations();
                    for (int j = 0; j < interpellations.size(); j++) {
                        MissionInterpellation interpellation = interpellations.get(j);
                        MissionInterpellation serviceInterpellation = serviceInterpellations.get(j);

                        synchronized (mRetryInterpellations) {
                            if (mRetryInterpellations.containsKey(interpellation.getLocalId())) {
                                report.getInterpellations().set(j, mRetryInterpellations.get(interpellation.getLocalId()));
                            } else {
                                interpellation.setSync(serviceInterpellation.isSync() || serviceInterpellation.isSyncing());
                            }
                        }
                    }
                }

                CacheManager.getInstance().saveCache(context);
            }

            setUserSynced(NetworkManager.getInstance().getUsername(), cacheData.isSynced());
        }
    }

    private void checkExistingTasks(String username) {
        int userTaskNumber;

        synchronized (mTaskLock) {
            Integer number = mTaskNumber.get(username);
            if (number == null) {
                number = 1;
            }

            number--;
            mTaskNumber.put(username, number);

            userTaskNumber = number;
        }

        if (userTaskNumber == 0) {
            new Handler(getApplication().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.summary_reports_sent, Toast.LENGTH_LONG).show();
                }
            });

            setUserSynced(username, true);
        }

        checkServiceNeeded();
    }

    public void checkServiceNeeded() {
        int number = 0;
        JSONObject usersSynced = SynchronisationService.getUsersSynced(this);
        Iterator<String> keys = usersSynced.keys();
        while(keys.hasNext()) {
            String username = keys.next();
            boolean synced = false;

            try {
                synced = usersSynced.getJSONObject(username).getBoolean("synced");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!synced) {
                number++;
            }
        }

        if (number == 0) {
            stopForeground(true);
        }
    }

    private NetworkConfiguration getNetworkConfiguration(final String username) {
        UserData userData = mData.get(username);
        if (userData != null && userData.getCacheData() != null) {
            return userData.getCacheData().getNetworkConfiguration();
        } else {
            return null;
        }
    }

    public class LocalBinder extends Binder {
        public SynchronisationService getService() {
            return SynchronisationService.this;
        }
    }

    public class UserData {
        private String mUsername;
        private CacheData mCacheData;
        private PendingIntent mAlarmIntent;

        public UserData() {
            super();
        }

        public String getUsername() {
            return mUsername;
        }

        public void setUsername(String username) {
            mUsername = username;
        }

        public CacheData getCacheData() {
            return mCacheData;
        }

        public void setCacheData(CacheData cacheData) {
            mCacheData = cacheData;
        }

        public PendingIntent getAlarmIntent() {
            return mAlarmIntent;
        }

        public void setAlarmIntent(PendingIntent alarmIntent) {
            mAlarmIntent = alarmIntent;
        }
    }
}
