package com.ipsis.scan.reporting.communication;

import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.communication.sending.RequestResponse;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.MissionFormsBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by pobouteau on 10/10/16.
 */

public class FormUpdateRequest {

    public void update(final Callback callback) {
        HashMap<String, Object> body = new HashMap<>();
        body.put("interpellation_form_id", 0);
        body.put("versions", "{}");

        NetworkManager.getInstance().post("/form/update", body).asJson(new RequestResponse.JsonCallback() {
            @Override
            public void onResponse(JSONObject json) {
                try {
                    if (json.getString("status").equals("success")) {
                        int interpellationFormId = json.getJSONObject("data").getInt("interpellationFormId");
                        String commentEmail = json.getJSONObject("data").getString("commentEmail");
                        String address = json.getJSONObject("data").getString("address");
                        JSONArray forms = json.getJSONObject("data").getJSONArray("forms");

                        CacheManager.getInstance().getData().setMissionForms(MissionFormsBuilder.parse(interpellationFormId, forms));
                        CacheManager.getInstance().getData().setCommentEmail(commentEmail);
                        CacheManager.getInstance().getData().setAddress(address);

                        callback.onUpdateSucceed();
                    } else {
                        callback.onUpdateFailure(new FormUpdateParseException());
                    }
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
    public interface Callback {
        /**
         * Called when forms are updated
         */
        void onUpdateSucceed();

        /**
         * Called when an error happens
         * @param e execption
         */
        void onUpdateFailure(Exception e);
    }
}
