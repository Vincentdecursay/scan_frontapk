package com.ipsis.scan.reporting.communication;

import android.accounts.NetworkErrorException;
import android.util.Log;
import com.ipsis.scan.communication.sending.NetworkConfiguration;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.communication.sending.RequestResponse;
import com.ipsis.scan.reporting.edition.model.MissionInterpellation;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.reporting.edition.model.MissionValue;
import com.ipsis.scan.utils.DateUtils;
import com.ipsis.scan.utils.SerializableSparseArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pobouteau on 10/10/16.
 */

public class SynchronisationRequest {

    private NetworkConfiguration mNetworkConfiguration;

    public SynchronisationRequest(NetworkConfiguration networkConfiguration) {
        super();

        mNetworkConfiguration = networkConfiguration;
    }

    public void sendReport(MissionReport missionReport, final Callback callback) {
        HashMap<String, Object> body = new HashMap<>();
        ArrayList<HashMap<String, Object>> values = new ArrayList<>();
        body.put("location", missionReport.getLocation().toString());
        body.put("location_user", missionReport.getLocationUser());
        body.put("location_start", missionReport.getLocationStart());
        body.put("location_end", missionReport.getLocationEnd());
        body.put("datetime_start", DateUtils.hourToUTC(missionReport.getDateTimeStart()));
        body.put("datetime_end", DateUtils.hourToUTC(missionReport.getDateTimeEnd()));
        body.put("form_id", String.valueOf(missionReport.getFormId()));
        body.put("temp_id", missionReport.getTempId());
        body.put("generated", missionReport.isSyncByUser() ? 0 : 1);

        SerializableSparseArray<MissionValue> missionValues = missionReport.getMissionValues();
        for(int i = 0; i < missionValues.size(); i++) {
            MissionValue missionValue = missionValues.get(missionValues.keyAt(i));
            HashMap<String, Object> value = new HashMap<>();
            value.put("field_id", String.valueOf(missionValue.getFieldId()));
            value.put("value", missionValue.getValue());
            values.add(value);
        }
        body.put("values", values);


        /*new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                callback.onSyncFailure(new NetworkErrorException("fake error report"));
            }
        }.start();*/

        NetworkManager.getInstance().post(mNetworkConfiguration, "/report/add", body).asJson(new RequestResponse.JsonCallback() {
            @Override
            public void onResponse(JSONObject json) {
                try {
                    if (json.getString("status").equals("success")) {
                        callback.onSyncSucceed();
                    } else {
                        callback.onSyncFailure(new NetworkErrorException(json.toString()));
                    }
                } catch (Exception e) {
                    callback.onSyncFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onSyncFailure(e);
            }
        });
    }

    public void sendInterpellation(MissionInterpellation missionInterpellation, final Callback callback) {
        HashMap<String, Object> body = new HashMap<>();

        ArrayList<HashMap<String, Object>> values = new ArrayList<>();

        body.put("location", missionInterpellation.getLocation().toString());
        body.put("location_user", missionInterpellation.getLocationUser());
        body.put("location_start", missionInterpellation.getLocationStart());
        body.put("location_end", missionInterpellation.getLocationEnd());
        body.put("datetime_start", DateUtils.hourToUTC(missionInterpellation.getDateTimeStart()));
        body.put("form_id", missionInterpellation.getFormId());
        body.put("report_temp_id", missionInterpellation.getTempId());
        body.put("generated", missionInterpellation.isSyncByUser() ? 0 : 1);

        SerializableSparseArray<MissionValue> missionValues = missionInterpellation.getMissionValues();
        for(int i = 0; i < missionValues.size(); i++) {
            MissionValue missionValue = missionValues.get(missionValues.keyAt(i));
            HashMap<String, Object> value = new HashMap<>();
            value.put("field_id", String.valueOf(missionValue.getFieldId()));
            value.put("value", missionValue.getValue());
            values.add(value);
        }
        body.put("values", values);

        Log.e("json", body.toString());

        NetworkManager.getInstance().post(mNetworkConfiguration, "/interpellation/add", body).asJson(new RequestResponse.JsonCallback() {
            @Override
            public void onResponse(JSONObject json) {
                try {
                    if (json.getString("status").equals("success")) {
                        callback.onSyncSucceed();
                    } else {
                        callback.onSyncFailure(new NetworkErrorException(json.toString()));
                    }
                } catch (Exception e) {
                    callback.onSyncFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onSyncFailure(e);
            }
        });
    }

    /**
     * Request callback
     */
    public interface Callback {
        /**
         * Called when the sync succeed
         */
        void onSyncSucceed();

        /**
         * Called when an error happens
         * @param e execption
         */
        void onSyncFailure(Exception e);
    }
}
