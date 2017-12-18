package com.ipsis.scan.database;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

/**
 * Created by pobouteau on 10/14/16.
 */

public class PlaceDataSource implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;

    private String mSearch;

    private PlaceResultCallback mPlaceResultCallback;

    public PlaceDataSource(Context context) {
        super();

        mGoogleApiClient = new GoogleApiClient
                .Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void onStart() {
        mGoogleApiClient.connect();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mSearch != null && mPlaceResultCallback != null) {
            search(mSearch, mPlaceResultCallback);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void search(String search, final PlaceResultCallback placeResultCallback) {
        if (mGoogleApiClient.isConnected()) {
            if (search.length() >= 2) {
                LatLngBounds bounds = new LatLngBounds(new LatLng(48.510782d, 1.856219d), new LatLng(49.123673d, 2.801409d));

                /*AutocompleteFilter.Builder typeFilter = new AutocompleteFilter.Builder();
                typeFilter.setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES);
                typeFilter.setCountry("FR");*/

                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, search, bounds, null)
                        .setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                            @Override
                            public void onResult(@NonNull AutocompletePredictionBuffer buffer) {
                                if (buffer.getStatus().isSuccess()) {
                                    placeResultCallback.onPlaceResult(buffer);
                                }

                                buffer.release();
                            }
                        });
            }
        } else {
            mSearch = search;
            mPlaceResultCallback = placeResultCallback;
        }

    }

    public interface PlaceResultCallback {
        void onPlaceResult(AutocompletePredictionBuffer locations);
    }
}
