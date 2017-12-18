package com.ipsis.scan.reporting.data;

import android.content.Context;
import android.util.Log;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.encryption.EncryptionManager;
import com.ipsis.scan.reporting.communication.service.SynchronisationClient;
import com.ipsis.scan.utils.DateUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pobouteau on 9/29/16.
 */
public class CacheManager {
    /**
     * Instance
     */
    private static CacheManager mInstance;

    /**
     * Cache data
     */
    private CacheData mData;

    /**
     * Is in offline mode
     */
    private boolean mOfflineMode;

    private final ArrayList<ConnectionStateListener> mConnectionStateListeners;

    /**
     * Constructor
     */
    private CacheManager() {
        super();

        mOfflineMode = false;
        mConnectionStateListeners = new ArrayList<>();
    }

    public static synchronized CacheManager getInstance() {
        if (mInstance == null) {
            mInstance = new CacheManager();
        }

        return mInstance;
    }

    /**
     * Load cache from memory
     * @param context Application context
     * @param callback
     */
    public void loadCache(final Context context, final CacheLoaderCallback callback) {
        EncryptionManager encryptionManager = EncryptionManager.getInstance(context);
        encryptionManager.decryptFromUserFile(NetworkManager.getInstance().getUsername(), "cache.bin", new EncryptionManager.AsyncFileDecryptCallback() {
            @Override
            public void onDecrypted(byte[] data) {
                if (data != null) {
                    try {
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                        ObjectInput input = new ObjectInputStream(inputStream);

                        final CacheData cacheData = (CacheData) input.readObject();
                        final Calendar date = Calendar.getInstance();

                        mData = cacheData;

                        if (cacheData.getCacheVersion() == CacheData.VERSION && cacheData.getMissionForms() != null && cacheData.getMissionForms().getMissionTypes().size() > 0) {
                        //if (false) { // TODO if (DateUtils.isSameDay(cacheData.getDate(), date)) {
                            final SynchronisationClient client = new SynchronisationClient(context);
                            client.updateLocalData(context, new SynchronisationClient.NextCallback() {
                                @Override
                                public void next() {
                                    client.disconnect();

                                    if (DateUtils.isSameDay(cacheData.getDate(), date)) {
                                        callback.onCacheLoaded();
                                    } else {
                                        callback.onCacheInvalidated();
                                    }
                                }
                            });
                        } else {
                            mData = new CacheData();

                            callback.onCacheError();
                        }

                        input.close();
                    } catch (Exception e) {
                        e.printStackTrace();

                        mData = new CacheData();

                        callback.onCacheError();
                    }
                } else {
                    mData = new CacheData();

                    callback.onCacheError();
                }
            }
        });
    }

    /**
     * Save cache (async)
     * @param context Application context
     */
    public void saveCache(Context context) {
        saveCache(context, null);
    }

    public void saveCache(Context context, CacheSavedCallback cacheSavedCallback) {
        new SaveCacheTask(context, cacheSavedCallback).start();
    }

    public boolean isOfflineMode() {
        return mOfflineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        mOfflineMode = offlineMode;

        synchronized (mConnectionStateListeners) {
            for (ConnectionStateListener connectionStateListener : mConnectionStateListeners) {
                connectionStateListener.onConnectedStateChanged(!offlineMode);
            }
        }
    }

    public void registerConnectionStateListener(ConnectionStateListener listener) {
        synchronized (mConnectionStateListeners) {
            mConnectionStateListeners.add(listener);
        }
    }

    public void unregisterConnectionStateListener(ConnectionStateListener listener) {
        synchronized (mConnectionStateListeners) {
            mConnectionStateListeners.remove(listener);
        }
    }

    public CacheData getData() {
        return mData;
    }

    public interface CacheLoaderCallback {
        void onCacheLoaded();
        void onCacheInvalidated();
        void onCacheError();
    }

    public interface ConnectionStateListener {
        void onConnectedStateChanged(boolean online);
    }

    public interface CacheSavedCallback {
        void onCacheSaved();
    }

    public interface CacheClosedCallback {
        void onCacheClosed();
    }

    private class SaveCacheTask extends Thread {

        private Context mContext;

        private CacheSavedCallback mCacheSavedCallback;

        public SaveCacheTask(Context context, CacheSavedCallback cacheSavedCallback) {
            super();

            mContext = context;
            mCacheSavedCallback = cacheSavedCallback;
        }

        @Override
        public void run() {
            EncryptionManager encryptionManager = EncryptionManager.getInstance(mContext);

            mData.setNetworkConfiguration(NetworkManager.getInstance().getNetworkConfiguration());

            SynchronisationClient synchronisationClient = new SynchronisationClient(mContext.getApplicationContext());
            synchronisationClient.updateServiceData();

            try {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutput output = new ObjectOutputStream(outputStream);
                output.writeObject(mData);
                output.flush();

                byte[] data = outputStream.toByteArray();
                encryptionManager.encryptToUserFile(NetworkManager.getInstance().getUsername(), "cache.bin", data, new EncryptionManager.AsyncFileEncryptCallback() {
                    @Override
                    public void onEncrypted(boolean error) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (mCacheSavedCallback != null) {
                            mCacheSavedCallback.onCacheSaved();
                        }

                        Log.e("cache", "saved");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();

                if (mCacheSavedCallback != null) {
                    mCacheSavedCallback.onCacheSaved();
                }
            }
        }
    }
}
