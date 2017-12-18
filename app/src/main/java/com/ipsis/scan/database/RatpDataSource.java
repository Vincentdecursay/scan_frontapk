package com.ipsis.scan.database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import com.ipsis.scan.BuildConfig;
import com.ipsis.scan.database.model.Route;
import com.ipsis.scan.database.model.Stop;
import com.ipsis.scan.utils.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by etude on 15/10/15.
 */
public class RatpDataSource {

    /**
     * Tag pour les logs.
     */
    private static final String TAG = RatpDataSource.class.getSimpleName();

    public static final String VERSION_PREFERENCES = "VersionPreferences";

    public static final String VERSION_PREFERENCE = "version";

    public static final int VERSION = 1;

    /**
     * SQLite Helper (création base...)
     */
    private SQLiteHelper mHelper;

    /**
     * Database manager
     */
    private SQLiteDatabase mDatabase;

    private SharedPreferences mPreferences;

    private boolean mProcessingRatpData;
    private final Object mProcessingRatpDataLocker;
    public static final double DELTA_LAT = 0.003d;
    public static final double DELTA_LNG = 0.004d;

    public RatpDataSource(Context context) {
        mHelper = new SQLiteHelper(context);

        mPreferences = context.getSharedPreferences(VERSION_PREFERENCES, Context.MODE_PRIVATE);

        mProcessingRatpData = false;
        mProcessingRatpDataLocker = new Object();
    }

    /**
     * Ouverture de la connexion
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        mDatabase = mHelper.getWritableDatabase();
    }

    /**
     * Fermeture de la connexion
     */
    public void close() {
        mHelper.close();
    }

    /**
     * Recherche une liste de ligne en fonction du nom court ou long
     *
     * @param search Terme
     * @return List de Route, peut être vide
     */
    public List<Route> searchRoute(String search) {
        String term = search;

        String[] tokens = search.split("/");
        if (tokens.length == 2) {
            term = tokens[0];
        }

        List<Route> routes = new ArrayList<>();

        if (term.length() > 0) {
            String sql = "";
            Cursor cursor;

            open();

            // resultats route_short_name quand term.length() <= 2
            if (term.length() <= 3) {
                sql = "SELECT * FROM routes";
                sql += " WHERE (route_short_name = \"" + term + "\")";
                sql += " ORDER BY LENGTH(route_short_name) ASC";

                cursor = mDatabase.rawQuery(sql, null);
                if (cursor.moveToFirst()) {
                    do {
                        Route route = parseRoute(cursor);

                        if (!routes.contains(route)) {
                            routes.add(route);
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            // resultats avec route_short_name
            sql = "SELECT * FROM routes";
            sql += " WHERE (route_short_name LIKE \"%" + term + "%\" OR route_long_name LIKE \"%" + term + "%\") AND route_short_name != \"\"";
            sql += " GROUP BY route_short_name";
            sql += " ORDER BY LENGTH(route_short_name) ASC";

            if (term.length() <= 2) {
                //sql += " LIMIT 0,5";
            }

            cursor = mDatabase.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    Route route = parseRoute(cursor);

                    if (!routes.contains(route)) {
                        routes.add(route);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            // resultats sans route_short_name
            sql = "SELECT * FROM routes";
            sql += " WHERE (route_short_name LIKE \"%" + term + "%\" OR route_long_name LIKE \"%" + term + "%\") AND route_short_name == \"\"";
            sql += " ORDER BY LENGTH(route_short_name) ASC";

            if (term.length() <= 2) {
                sql += " LIMIT 0,5";
            }

            cursor = mDatabase.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    Route route = parseRoute(cursor);

                    if (!routes.contains(route)) {
                        routes.add(route);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            close();
        }

        return routes;
    }

    /**
     * Parse le cursor en Route
     *
     * @param cursor cursor placé
     * @return Route
     */
    private Route parseRoute(Cursor cursor) {
        String routeId = cursor.getString(cursor.getColumnIndex("route_id"));
        String routeShortName = cursor.getString(cursor.getColumnIndex("route_short_name"));
        String routeLongName = cursor.getString(cursor.getColumnIndex("route_long_name"));
        String type = cursor.getString(cursor.getColumnIndex("type"));

        if (type.equals("ratp")) {
            return Route.fromRatp(routeId, routeShortName, routeLongName);
        } else if (type.equals("transilien")) {
            return Route.fromTransilien(routeId, routeShortName, routeLongName);
        } else if (type.equals("optile")) {
            return Route.fromOptile(routeId, routeShortName, routeLongName);
        } else if (type.equals("stif")) {
            return Route.fromStif(routeId, routeShortName, routeLongName);
        } else {
            return Route.fromRatp(routeId, routeShortName, routeLongName);
        }
    }

    /**
     * Recherche la liste des arrêts associés à une ligne
     *
     * @param route Ligne
     * @return List de Stop
     */
    public List<Stop> getStopsByRoute(Route route) {
        List<Stop> stops = new ArrayList<>();

        String sql = "";
        sql += "SELECT * FROM stops_by_route";
        sql += " WHERE route_id = '" + route.getRouteId() + "'";
        sql += " GROUP BY stop_name";
        sql += " ORDER BY id ASC";

        open();

        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Stop stop = parseStop(cursor);

                stops.add(stop);
            } while (cursor.moveToNext());
        }
        cursor.close();

        close();

        return stops;
    }

    public List<Stop> searchStop(String term) {
        List<Stop> stops = new ArrayList<>();

        if (term.length() > 0) {
            String sql = "";
            sql += "SELECT * FROM stops_by_route";
            sql += " WHERE stop_name LIKE \"%" + term + "%\"";
            //sql += " WHERE stop_name LIKE \"%" + term + "%\" OR stop_desc LIKE \"%" + term + "%\"";
            //sql += " ORDER BY LENGTH(route_short_name) ASC";
            // sql += " GROUP BY stop_desc";

            if (term.length() <= 3) {
                sql += " LIMIT 0,5";
            }

            open();

            Cursor cursor = mDatabase.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    Stop stop = parseStop(cursor);

                    stops.add(stop);
                } while (cursor.moveToNext());
            }
            cursor.close();

            close();
        }

        return stops;
    }

    /*
            lat +- 0.0045
            long +- 0.0065
            about 500m

         +lat,-long                          +lat,+long
         48.8733931,2.3301962                48.8733931,2.3431962
                            48.8688931,2.3366962
         48.8643931,2.3301962                48.8643931,2.3431962
         -lat,-long                          -lat,+long
     */

    public List<Stop> searchStop(Location location) {
        List<Stop> stops = new ArrayList<>();

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        if (BuildConfig.DEBUG) {
            lat = 48.8688931d; // TODO remove fake location
            lng = 2.3366962d;
        }

        double topLat = lat + DELTA_LAT;
        double bottomLat = lat - DELTA_LAT;
        double leftLng = lng - DELTA_LNG;
        double rightLng = lng + DELTA_LNG;

        String sql = "";
        sql += "SELECT * FROM stops_by_route";
        sql += " WHERE stop_lat >= " + bottomLat + " AND stop_lat <= " + topLat;
        sql += " AND stop_lon >= " + leftLng + " AND stop_lon <= " + rightLng;
        // sql += " GROUP BY stop_desc";

        open();

        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                Stop stop = parseStop(cursor);

                stops.add(stop);
            } while (cursor.moveToNext());
        }
        cursor.close();

        close();

        return stops;
    }

    /**
     * Parse le cursor en Stop
     *
     * @param cursor cursor placé
     * @return Stop
     */
    private Stop parseStop(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex("stop_name"));
        String desc = cursor.getString(cursor.getColumnIndex("stop_desc"));
        double latitude = cursor.getDouble(cursor.getColumnIndex("stop_lat"));
        double longitude = cursor.getDouble(cursor.getColumnIndex("stop_lon"));

        return new Stop(name, desc, latitude, longitude);
    }

    /**
     * Insertion de données avec fichier
     * 1 requête par ligne
     * 3.85783333333 minutes sur galaxy s4
     *
     */
    public void update(ProgressDialog progressDialog, File file, int version) throws IOException {
        synchronized (mProcessingRatpDataLocker) {
            mProcessingRatpData = true;
        }

        Log.e("time start", "" + System.currentTimeMillis());

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int i = 0;

        open();

        mDatabase.execSQL("DELETE FROM routes");
        mDatabase.execSQL("DELETE FROM stops_by_route");

        while ((line = reader.readLine()) != null) {
            mDatabase.execSQL(line);

            if (i % 400 == 0) {
                if (progressDialog != null) {
                    progressDialog.setProgress(((i * 100) / 37852));
                }

                Log.e("insert", ((i * 100) / 37852) + "%");
            }

            i++;
        }
        reader.close();

        close();

        setVersion(version);

        if (progressDialog != null) {
            progressDialog.setProgress(100);
        }

        Log.e("time end", "" + System.currentTimeMillis());

        synchronized (mProcessingRatpDataLocker) {
            mProcessingRatpData = false;
        }
    }

    public boolean isProcessingRatpData() {
        synchronized (mProcessingRatpDataLocker) {
            return mProcessingRatpData;
        }
    }

    public int getVersion() {
        return mPreferences.getInt(VERSION_PREFERENCE, -1);
    }

    public void setVersion(int version) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(VERSION_PREFERENCE, version);
        editor.apply();
    }
}
