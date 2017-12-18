package com.ipsis.scan.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.ipsis.scan.R;

/**
 * Created by etude on 19/10/15.
 */
public class AppUtils {
    public static ColorStateList createEditTextColorStateList(@NonNull Context context, @ColorInt int color) {
        int[][] states = new int[3][];
        int[] colors = new int[3];
        int i = 0;
        states[i] = new int[]{-android.R.attr.state_enabled};
        colors[i] = color;
        i++;
        states[i] = new int[]{-android.R.attr.state_pressed, -android.R.attr.state_focused};
        colors[i] = color;
        i++;
        states[i] = new int[]{};
        colors[i] = color;
        return new ColorStateList(states, colors);
    }

    public static float convertDpToPixel(Context context, float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void justifyListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    /**
     * Définie les paramètres de layout à utiliser pour la vue passée en paramètre avec les
     * margins spécifiées.
     *
     * @param view   vue
     * @param context    contexte
     * @param width  paramètre de largeur
     * @param height paramètre de hauteur
     * @param marginStart  margin gauche
     * @param marginTop  margin haut
     * @param marginEnd  margin droite
     * @param marginBottom  margin bas
     */
    public static void setLinearLayoutParams(View view, Context context, int width, int height,
                                              int marginStart, int marginTop, int marginEnd, int marginBottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        int left = dpToPx(context, marginStart);
        int top = dpToPx(context, marginTop);
        int right = dpToPx(context, marginEnd);
        int bottom = dpToPx(context, marginBottom);
        params.setMargins(left, top, right, bottom);
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);
    }


    public static void setLinearLayoutWeight(View view, float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.setLayoutParams(params);
    }


    /**
     * Définie les paramètres de layout à utiliser pour la vue passée en paramètre avec les
     * margins spécifiées.
     *
     * @param view   vue
     * @param context    contexte
     * @param width  paramètre de largeur
     * @param height paramètre de hauteur
     * @param marginStart  margin gauche
     * @param marginTop  margin haut
     * @param marginEnd  margin droite
     * @param marginBottom  margin bas
     */
    public static void setRelativeLayoutParams(View view, Context context, int width, int height,
                                                int marginStart, int marginTop, int marginEnd, int marginBottom) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        int left = dpToPx(context, marginStart);
        int top = dpToPx(context, marginTop);
        int right = dpToPx(context, marginEnd);
        int bottom = dpToPx(context, marginBottom);
        params.setMargins(left, top, right, bottom);
        view.setLayoutParams(params);
    }

    public static void setRelativeLayoutRule(View view, int rule) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.addRule(rule);
        view.setLayoutParams(params);
    }

    /**
     * Renvoie l'identifiant dans les resources de l'icone qui a le nom passé en paramètre
     *
     * @param iconName
     * @param context
     * @return
     */
    public static int getIconResource(Context context, String iconName) {
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getSelectableBackgroundId(Context context) {
        //Get selectableItemBackgroundBorderless defined for AppCompat
        int colorAttr = context.getResources().getIdentifier("selectableItemBackgroundBorderless", "attr", context.getPackageName());

        if (colorAttr == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                colorAttr = android.R.attr.selectableItemBackgroundBorderless;
            } else {
                colorAttr = android.R.attr.selectableItemBackground;
            }
        }

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.resourceId;
    }

    /**
     * Build an error dialog
     * @param message string id
     */
    public static void showErrorDialog(Context context, int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setPositiveButton(R.string.button_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }
}
