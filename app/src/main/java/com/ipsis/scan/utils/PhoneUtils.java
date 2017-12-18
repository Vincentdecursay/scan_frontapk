package com.ipsis.scan.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by pobouteau on 9/28/16.
 */

public class PhoneUtils {
    public static String getImei(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
