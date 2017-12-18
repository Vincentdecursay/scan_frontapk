package com.ipsis.scan.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Random;

/**
 * Created by etude on 15/10/15.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    /**
     * Version de la base de données locale
     */
    private static final int DATABASE_VERSION = 16;

    /**
     * Table routes
     */
    private static final String DATABASE_CREATE_ROUTES = "CREATE TABLE `routes` (" +
            "`route_id` varchar(50) NOT NULL," +
            "`agency_id` varchar(50) DEFAULT NULL," +
            "`route_short_name` varchar(50) DEFAULT NULL," +
            "`route_long_name` varchar(255) DEFAULT NULL," +
            "`route_desc` varchar(255) DEFAULT NULL," +
            "`route_type` int(2) DEFAULT NULL," +
            "`route_url` varchar(255) DEFAULT NULL," +
            "`route_color` varchar(255) DEFAULT NULL," +
            "`route_text_color` varchar(255) DEFAULT NULL," +
            "`type` varchar(255) DEFAULT NULL," +
            "PRIMARY KEY (`route_id`)" +
            ");";

    /**
     * Table stops_by_route
     */
    private static final String DATABASE_CREATE_STOPS_BY_ROUTE = "CREATE TABLE `stops_by_route` (" +
            "  `id` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  `stop_id` varchar(50) DEFAULT NULL," +
            "  `route_id` varchar(50) DEFAULT NULL," +
            "  `stop_name` varchar(255) DEFAULT NULL," +
            "  `stop_desc` varchar(255) DEFAULT NULL," +
            "  `stop_lat` decimal(8,6) DEFAULT NULL," +
            "  `stop_lon` decimal(8,6) DEFAULT NULL," +
            "  `stop_sequence` int(4) DEFAULT NULL" +
            ");";

    public SQLiteHelper(Context context) {
        super(context, "ratp-data.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_ROUTES);
        database.execSQL(DATABASE_CREATE_STOPS_BY_ROUTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS routes");
        db.execSQL("DROP TABLE IF EXISTS stops_by_route");
        onCreate(db);
    }
}
