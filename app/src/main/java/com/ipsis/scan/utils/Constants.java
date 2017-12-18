package com.ipsis.scan.utils;

import android.content.Context;
import com.ipsis.scan.R;

/**
 * Created by po on 18/10/16.
 */

public class Constants {
    public static final int TYPE_SUR_LIGNE = 0;

    public static final int TYPE_LIEU_FIXE = 1;

    public static final int TYPE_HORS_RESEAU = 2;

    public static final String FORM_SUR_LIGNE = "sur-ligne";

    public static final String FORM_LIEU_FIXE = "lieu-fixe";

    public static final String FORM_HORS_RESEAU = "hors-reseau";

    public static final String INTERPELLATION_DISABLED = "0";

    public static final String INTERPELLATION_ENABLED = "1";

    public static final int TYPE_SEARCH_ROUTE = 0;

    public static final int TYPE_SEARCH_STOP = 1;

    public static final int TYPE_SEARCH_LOCATION = 2;

    public static String getMissionFormType(Context context, String formType) {
        if (formType.equals(FORM_SUR_LIGNE)) {
            return context.getString(R.string.create_sur_ligne);
        } else if (formType.equals(FORM_LIEU_FIXE)) {
            return context.getString(R.string.create_lieu_fixe);
        } else {
            return context.getString(R.string.create_hors_reseau);
        }
    }
}
