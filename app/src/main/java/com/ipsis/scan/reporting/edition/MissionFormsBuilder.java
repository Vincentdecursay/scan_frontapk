package com.ipsis.scan.reporting.edition;

import android.content.Context;
import com.ipsis.scan.reporting.edition.model.MissionField;
import com.ipsis.scan.reporting.edition.model.MissionFormData;
import com.ipsis.scan.reporting.edition.model.MissionType;
import com.ipsis.scan.utils.SerializableSparseArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by pobouteau on 9/30/16.
 */

public class MissionFormsBuilder {

    public static MissionForms parse(Context context, int interpellationFormId, String name) throws JSONException {
        JSONArray json = getJSONFromFile(context, name);

        return parse(interpellationFormId, json);
    }

    public static MissionForms parse(int interpellationFormId, JSONArray json) throws JSONException {
        MissionForms result = null;

        if (json != null) {
            MissionType interpellationMissionType = null;
            ArrayList<MissionType> missionTypes = new ArrayList<>();

            for (int i = 0 ; i < json.length() ; i++) {
                MissionType missionType = parseType(json.getJSONObject(i));
                if (missionType.getForm().getId() != interpellationFormId) {
                    missionTypes.add(missionType);
                } else {
                    interpellationMissionType = missionType;
                }
            }

            result = new MissionForms(interpellationMissionType, missionTypes);
        }

        return result;
    }

    private static MissionType parseType(JSONObject type) throws JSONException {
        MissionType missionType = new MissionType(type.getInt("id"), type.getString("name"), type.getInt("version"));
        missionType.setForm(parseForm(type.getJSONObject("form")));
        missionType.setFields(parseFields(type.getJSONArray("fields")));
        return missionType;
    }

    private static MissionFormData parseForm(JSONObject form) throws JSONException {
        return new MissionFormData(form.getInt("id"), form.getString("locationMode"), form.getString("interpellationEnable"));
    }

    private static SerializableSparseArray<SerializableSparseArray<MissionField>> parseFields(JSONArray fields) throws JSONException {
        SerializableSparseArray<SerializableSparseArray<MissionField>> missionFields = new SerializableSparseArray<>();

        for (int i = 0 ; i < fields.length() ; i++) {
            MissionField missionField = parseField(fields.getJSONObject(i));

            SerializableSparseArray<MissionField> group = missionFields.get(missionField.getGroup());
            if (group == null) {
                group = new SerializableSparseArray<>();

                missionFields.append(missionField.getGroup(), group);
            }

            group.append(missionField.getOrder(), missionField);
        }

        return missionFields;
    }

    private static MissionField parseField(JSONObject field) throws JSONException {
        MissionField missionField = new MissionField();

        missionField.setId(field.getInt("id"));
        missionField.setType(field.getString("type"));
        missionField.setLabel(field.getString("label"));
        missionField.setValue(field.getString("value"));
        missionField.setGroup(field.getInt("group"));
        missionField.setOrder(field.getInt("order"));
        missionField.setIcon(field.getString("icon"));

        return missionField;
    }

    /**
     * Renvoie l'objet JSON correspondant au contenu du fichier
     */
    private static JSONArray getJSONFromFile(Context context, String name) {
        JSONArray result = null;
        try {
            result = new JSONArray(readFile(context, name));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Renvoie le contenu du fichier passé en paramètre sous forme d'une chaine de caractères.
     *
     * @return le contenu du fichier passé en paramètre sous forme d'une chaine de caractères
     */
    private static String readFile(Context context, String name) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(name)));
            String line = reader.readLine();
            while (line != null) {
                content.append(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return content.toString();
    }
}
