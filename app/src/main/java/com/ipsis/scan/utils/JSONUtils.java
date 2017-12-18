package com.ipsis.scan.utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pobouteau on 9/30/16.
 */

public class JSONUtils {
    public static ArrayList<String> JSONArrayToStringArray(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        ArrayList<String> result = new ArrayList<>();

        for(int i = 0; i < array.length(); i++){
            result.add(array.getString(i));
        }

        return result;
    }
}
