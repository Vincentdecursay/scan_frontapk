package com.ipsis.scan.database;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.List;

/**
 * Created by pobouteau on 10/14/16.
 */

public class PlaceDataSource {
    private String mSearch;

    public PlaceDataSource(Context context) {
        super();

    }

    public void onStart() {

    }

    public void onStop() {

    }


    public void search(String search) {

        mSearch = search;

    }

}
