package com.ipsis.scan;

import android.app.Application;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

/**
 * Created by pobouteau on 10/25/16.
 */

public class ScanApplication extends MultiDexApplication {

    /*private static final Goro GORO = Goro.create();

    @Override
    public void onCreate() {
        super.onCreate();

        GoroService.setup(this, GORO);
    }*/

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        Log.e("trim", "" + level);
    }
}
