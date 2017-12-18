package com.ipsis.scan.geolocation;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static java.lang.Thread.sleep;

/**
 * <p/>
 * Description : Assure la gestion de la géolocalisation du mobile. Celui-ci est lié
 * à une activité. Les appels aux fonctions onStart(), onResume(), onPause() et onStop() doivent
 * être faits respectivement dans les fonctions onStart(), onResume(), onPause() et onStop() de
 * l'activité. ATTENTION : Cette activité doit gérer les résultats de retour d'une autre
 * activité pour la gestion des réglages concernant le localisation :
 * <pre>
 * {@code
 *
 * @author Maxime NATUREL
 * @version 1.0
 * @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 * switch (requestCode) {
 * // Check for the integer request code originally supplied to startResolutionForResult().
 * case LocationManager.REQUEST_CHECK_SETTINGS:
 * switch (resultCode) {
 * case Activity.RESULT_OK:
 * Log.i(TAG, "User agreed to make required location settings changes.");
 * mLocationManager.tryStartingLocationUpdates();
 * break;
 * case Activity.RESULT_CANCELED:
 * Log.i(TAG, "User chose not to make required location settings changes.");
 * break;
 * }
 * break;
 * }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * Copyright (c) 2015
 * </p>
 * <p>
 * Société : IPSIS
 * </p>
 * <p>
 * Date : 5 octobre 2015
 * </p>
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location
                .LocationListener, ResultCallback<LocationSettingsResult> {

    /**
     * Constante utilisée dans la boîte de dialogue pour les réglages concernant la localisation.
     */
    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Tag pour les logs.
     */
    private static final String TAG = LocationManager.class.getSimpleName();

    /**
     * Intervalles désirée pour la fréquence des mises à jour de la position.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * Fréquence maximale des mises à jour de la position.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Minimum accuracy to stop location updates
     */
    private static final float MIN_ACCURACY_IN_METERS = 25;

    /**
     * Minimum time to stop location updates
     */
    private static final long MIN_LOCATION_REQUEST_TIME_IN_MILLISECONDS = 1000 * 10;

    /**
     * Maximum time to stop location updates
     */
    private static final long MAX_LOCATION_REQUEST_TIME_IN_MILLISECONDS = 1000 * 40;

    /**
     * Singleton
     */
    private static LocationManager instance;

    private static boolean sAsked = false;

    private static int sAskedNumber = 0;

    /**
     * Activité liée au LocationManager.
     */
    private Activity mActivity;

    /**
     * Client Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Position courante.
     */
    private Location mCurrentLocation;

    /**
     * Définie les parmètres pour la mise à jour de la position.
     */
    private LocationRequest mLocationRequest;

    /**
     * Définie le type de service utilisé pour la géolocalisation. Utilisé pour vérifier les
     * réglages pour déterminer si l'appareil présente les réglages optimaux concernant la
     * localisation.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Indique si la mise à jour de la position est en cours.
     */
    private boolean mRequestingLocationUpdates;

    /**
     * Location update start time
     */
    private long mRequestingStartTime;

    /**
     * Stop location update task
     */
    private StopLocationUpdateTask mStopLocationUpdateTask;

    /**
     * @param activity activité qui a besoin de connaître la position
     */
    public LocationManager(Activity activity) {
        mActivity = activity;
        mRequestingLocationUpdates = false;

        buildGoogleApiClient(activity);
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();
    }

    /**
     * Etablie la connexion avec les services Google Play.
     *
     * @param context contexte utilisé pour la connexion avec les services Google Play
     */
    private synchronized void buildGoogleApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Définie les paramètres pour la mise à jour de la position.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.setAlwaysShow(true);
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Vérifie que les réglages de l'appareil concernant la localisation sont suffisants pour les
     * besoins de l'application.
     */
    private void checkLocationSettings() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    /**
     * Renvoie la position courante.
     *
     * @return la position courante.
     */
    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    /**
     * Réalise les actions qui doivent être faite dans l'appel à onStart d'une activité.
     */
    public void onStart() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Réalise les actions qui doivent être faite dans l'appel à onStop d'une activité.
     */
    public void onStop() {
        LocationManager.instance = null;

        stopLocationUpdates();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        Log.i(TAG, "Disconnected from GoogleApiClient");
    }

    /**
     * Réalise les actions qui doivent être faite dans l'appel à onResume d'une activité.
     */
    public void onResume() {
        // on démarre la mise à jour de la position si besoin
        //tryStartingLocationUpdates();
    }

    /**
     * Réalise les actions qui doivent être faite dans l'appel à onPause d'une activité.
     */
    public void onPause() {
        // on arrête la mise à jour de la position pour économiser la batterie
        /*if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }*/
    }

    /**
     * Démarre la mise à jour de la position à intervalles réguliers si possible.
     */
    public void tryStartingLocationUpdates() {
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Démarre la mise à jour de la position courante à intervalles réguliers.
     */
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
                .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.i(TAG, "Location updates started");
                mRequestingLocationUpdates = true;
                mRequestingStartTime = System.currentTimeMillis();
                mStopLocationUpdateTask = new StopLocationUpdateTask();

                mStopLocationUpdateTask.start();
            }
        });
    }

    /**
     * Arrête la mise à jour de la position courante à intervalles réguliers.
     */
    private void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
                    .setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    Log.i(TAG, "Location updates stopped");
                    mRequestingLocationUpdates = false;
                }
            });
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // on démarre la mise à jour de la position si besoin
        //tryStartingLocationUpdates();

        LocationManager.instance = this;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged : " + "lat : " + location.getLatitude() + ", lon : " + location.getLongitude());

        mCurrentLocation = location;

        long currentTime = System.currentTimeMillis();
        if (location.getAccuracy() <= MIN_ACCURACY_IN_METERS && currentTime - mRequestingStartTime > MIN_LOCATION_REQUEST_TIME_IN_MILLISECONDS) {
            stopLocationUpdates();
        }
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    sAskedNumber++;
                    if (sAskedNumber >= 6) {
                        sAskedNumber = 0;
                        sAsked = false;
                    }

                    if (!sAsked) {
                        status.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                        sAsked = true;
                    }
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    /**
     * Singleton
     * @param activity Current context
     * @return singleton
     */
    public static LocationManager getInstance(Activity activity) {
        if(instance == null) {
            instance = new LocationManager(activity);
        } else {
            instance.mActivity = activity;
        }

        return instance;
    }

    /**
     * Stop location update task
     * See MAX_LOCATION_REQUEST_TIME_IN_MILLISECONDS
     */
    private class StopLocationUpdateTask extends Thread {
        @Override
        public void run() {
            try {
                sleep(MAX_LOCATION_REQUEST_TIME_IN_MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stopLocationUpdates();
        }
    }
}
