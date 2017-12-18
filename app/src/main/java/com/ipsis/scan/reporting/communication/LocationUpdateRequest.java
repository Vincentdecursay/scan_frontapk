package com.ipsis.scan.reporting.communication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.communication.sending.RequestResponse;
import com.ipsis.scan.database.RatpDataSource;
import com.ipsis.scan.utils.UnzipUtils;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;

/**
 * Created by pobouteau on 10/10/16.
 */

public class LocationUpdateRequest {

    private int mVersion;

    public void checkForUpdate(final Context context, final CheckForUpdateCallback callback) {
        final RatpDataSource ratpDataSource = new RatpDataSource(context);
        final boolean localData = ratpDataSource.getVersion() >= 0;

        HashMap<String, Object> body = new HashMap<>();
        body.put("version", ratpDataSource.getVersion());

        NetworkManager.getInstance().post("/data/update", body).asJson(new RequestResponse.JsonCallback() {
            @Override
            public void onResponse(JSONObject json) {
                try {
                    if (json.getString("status").equals("success")) {
                        mVersion = json.getJSONObject("data").getInt("version");

                        if (mVersion != ratpDataSource.getVersion()) {
                            callback.onUpdateAvailable(localData);
                        } else {
                            callback.onNoUpdateAvailable();
                        }
                    } else {
                        callback.onUpdateFailure(localData, new Exception("Request error (data/update)"));
                    }
                } catch (Exception e) {
                    callback.onUpdateFailure(localData, e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onUpdateFailure(localData, e);
            }
        });
    }

    public void update(final ProgressDialog progressDialog, final UpdateCallback callback) {
        HashMap<String, Object> body = new HashMap<>();
        NetworkManager.getInstance().post("/data/file", body).asBinary(new RequestResponse.BinaryCallback() {
            @Override
            public void onResponse(InputStream inputStream) {
                byte[] buffer = new byte[1024 * 4];
                File databaseZip = new File(progressDialog.getContext().getCacheDir(), "ratp.sql.zip");
                //File databaseZip = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ratp.sql.zip");

                try {
                    OutputStream output = new FileOutputStream(databaseZip);

                    while (true) {
                        int read = inputStream.read(buffer);

                        if (read == -1) {
                            break;
                        }
                        output.write(buffer, 0, read);
                    }

                    output.flush();
                    output.close();

                    File destinationDirectory = new File(databaseZip.getParent());
                    File database = new File(destinationDirectory, "ratp.sql");

                    UnzipUtils.unzip(databaseZip, destinationDirectory);

                    RatpDataSource ratpDataSource = new RatpDataSource(progressDialog.getContext());
                    ratpDataSource.update(progressDialog, database, mVersion);

                    databaseZip.delete();
                    database.delete();

                    callback.onUpdateSucceed();
                } catch (Exception e) {
                    callback.onUpdateFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onUpdateFailure(e);
            }
        });
    }

    /**
     * Request callback
     */
    public interface CheckForUpdateCallback {
        /**
         * Called when an update is available
         */
        void onUpdateAvailable(boolean localData);

        void onNoUpdateAvailable();

        /**
         * Called when an error happens
         * @param e execption
         */
        void onUpdateFailure(boolean localData, Exception e);
    }

    public interface UpdateCallback {
        void onUpdateSucceed();

        void onUpdateFailure(Exception e);
    }
}
