package com.ipsis.scan.security.locking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.ipsis.scan.security.activities.LockActivity;

/**
 * Created by pobouteau on 10/10/16.
 */

public class LockManager {
    /**
     * Instance
     */
    private static LockManager sInstance;

    private static final String LOCK_PREFERENCES = "LockPreferences";

    private static final String ON_PAUSE_TIME_PREFERENCE = "onPauseDate";

    private static final long MAX_TIME_PAUSED = 2 * 1000;

    private static final long MAX_TIME_VISIBLE = 5 * 60 * 1000;

    private Activity mCurrentActivity;

    private LockTask mLockTask;

    private boolean mFirstTime;

    public LockManager() {
        mFirstTime = true;
    }

    public static LockManager getInstance() {
        if (sInstance == null) {
            sInstance = new LockManager();
        }

        return sInstance;
    }

    public void init(Activity activity) {
        onPause(activity);
    }

    public void onResume(Activity activity) {
        long currentTime = System.currentTimeMillis();
        long time = currentTime;
        if (!mFirstTime) {
            SharedPreferences preferences = activity.getSharedPreferences(LOCK_PREFERENCES, Context.MODE_PRIVATE);
            time = preferences.getLong(ON_PAUSE_TIME_PREFERENCE, currentTime + MAX_TIME_PAUSED);
        }
        mFirstTime = false;

        if (currentTime - time > MAX_TIME_PAUSED) {
            activity.startActivity(new Intent(activity, LockActivity.class));
        }

        mCurrentActivity = activity;

        if (mLockTask != null) {
            mLockTask.cancel();
        }
        mLockTask = new LockTask();
        mLockTask.start();
    }

    public void onPause(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(LOCK_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(ON_PAUSE_TIME_PREFERENCE, System.currentTimeMillis());
        editor.apply();

        if (mLockTask != null) {
            mLockTask.cancel();
        }
        mCurrentActivity = null;
    }

    public void reset() {
        if (mLockTask != null) {
            mLockTask.reset();
        }
    }

    public class LockTask extends Thread {
        public boolean mCanceled;

        public boolean mReset;

        public LockTask() {
            super();

            mCanceled = false;
            mReset = false;
        }

        public void cancel() {
            mCanceled = true;
        }

        public void reset() {
            mReset = true;
        }

        @Override
        public void run() {
            try {
                do {
                    mReset = false;

                    sleep(MAX_TIME_VISIBLE);
                } while(mReset);

                if (!mCanceled && mCurrentActivity != null) {
                    mCurrentActivity.startActivity(new Intent(mCurrentActivity, LockActivity.class));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
