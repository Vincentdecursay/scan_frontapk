package com.ipsis.scan.reporting.communication.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.reporting.data.CacheData;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.model.MissionInterpellation;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.utils.SerializationUtils;

import java.util.ArrayList;

/**
 * Created by pobouteau on 10/25/16.
 */

public class SynchronisationClient {

    private Context mContext;

    private boolean mIsBound;

    private SynchronisationService mService;

    private ServiceConnection mServiceConnection;

    public SynchronisationClient(Context context) {
        super();

        mContext = context;
    }

    public void connect(final NextCallback nextCallback) {
        if (mService == null) {
            mContext.startService(new Intent(mContext, SynchronisationService.class));

            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder binder) {
                    mService = ((SynchronisationService.LocalBinder) binder).getService();

                    nextCallback.next();
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mService = null;
                }
            };

            mContext.bindService(new Intent(mContext, SynchronisationService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

            mIsBound = true;
        } else {
            nextCallback.next();
        }
    }

    public void disconnect() {
        if (mIsBound) {
            mContext.unbindService(mServiceConnection);
            mIsBound = false;
        }
    }

    public void getData(final DataCallback dataCallback) {
        connect(new NextCallback() {
            @Override
            public void next() {
                dataCallback.onGetData(mService.getData());
            }
        });
    }

    public void updateLocalData(final Context context, final NextCallback nextCallback) {
        connect(new NextCallback() {
            @Override
            public void next() {
                mService.updateLocalData(context);

                nextCallback.next();
            }
        });
    }

    public void updateServiceData() {
        final CacheData cacheData = SerializationUtils.deepClone(CacheManager.getInstance().getData());

        connect(new NextCallback() {
            @Override
            public void next() {
                mService.updateServiceData(cacheData);

                disconnect();
            }
        });
    }

    public void sendReports(final ArrayList<MissionReport> missionReports) {
        connect(new NextCallback() {
            @Override
            public void next() {
                mService.sendReports(NetworkManager.getInstance().getNetworkConfiguration(), missionReports);
            }
        });
    }

    public void sendInterpellation(final MissionInterpellation missionInterpellation) {
        connect(new NextCallback() {
            @Override
            public void next() {
                mService.sendInterpellation(NetworkManager.getInstance().getNetworkConfiguration(), missionInterpellation);
            }
        });
    }

    public void scheduleSynchronisation() {
        connect(new NextCallback() {
            @Override
            public void next() {
                mService.scheduleSynchronisation();
            }
        });
    }

    public interface NextCallback {
        void next();
    }

    public interface DataCallback {
        void onGetData(CacheData cacheData);
    }
}
