package com.ipsis.scan.reporting.edition;

import android.content.Context;
import android.util.Log;
import com.ipsis.scan.R;
import com.ipsis.scan.reporting.data.CacheData;
import com.ipsis.scan.reporting.data.CacheManager;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.utils.AppUtils;
import com.ipsis.scan.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pobouteau on 10/19/16.
 */

public class MissionValidation {
    public static boolean isValidDateStart(Context context, MissionReport excludedReport, String date) {
        ArrayList<MissionReport> reports = CacheManager.getInstance().getData().getMissionReports();
        int compareDate = DateUtils.compareDate(date);

        if (compareDate != -1) {
            for (MissionReport report : reports) {
                if (report != excludedReport) {
                    int compareDateStart = report.getCompareDateStart();
                    int compareDateEnd = report.getCompareDateEnd();

                    if (compareDateEnd != -1 && compareDate >= compareDateStart && compareDate < compareDateEnd) {
                        AppUtils.showErrorDialog(context, R.string.edit_dialog_date_exists);

                        return false;
                    }
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isValidDateEnd(Context context, MissionReport excludedReport, String date) {
        ArrayList<MissionReport> reports = CacheManager.getInstance().getData().getMissionReports();
        int compareDate = DateUtils.compareDate(date);

        if (compareDate != -1) {
            for (MissionReport report : reports) {
                if (report != excludedReport) {
                    int compareDateStart = report.getTimelineDateStart();
                    int compareDateEnd = report.getTimelineDateEnd();

                    if (compareDateEnd != -1 && compareDate > compareDateStart && compareDate <= compareDateEnd) {
                        AppUtils.showErrorDialog(context, R.string.edit_dialog_date_exists);

                        return false;
                    }
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isValidDateStartEnd(Context context, MissionReport excludedReport, String dateStart, String dateEnd) {
        ArrayList<MissionReport> reports = CacheManager.getInstance().getData().getMissionReports();
        int compareDateStart = DateUtils.compareDate(dateStart);
        int compareDateEnd = DateUtils.compareDate(dateEnd);

        if (compareDateStart != -1 && compareDateEnd != -1) {
            if (compareDateStart <= compareDateEnd) {
                for (MissionReport report : reports) {
                    if (report != excludedReport) {
                        int reportDateStart = report.getTimelineDateStart();
                        int reportDateEnd = report.getTimelineDateEnd();

                        //Log.e("compare", "" + (compareDateStart < reportDateStart && compareDateEnd > reportDateStart));
                        //Log.e("compare2", "" + (compareDateStart < reportDateEnd && compareDateEnd > reportDateEnd));

                        if ((compareDateStart < reportDateStart && compareDateEnd > reportDateStart)
                                || (compareDateStart < reportDateEnd && compareDateEnd > reportDateEnd)) {
                            AppUtils.showErrorDialog(context, R.string.edit_dialog_date_exists);

                            return false;
                        }
                    }
                }

                return true;
            } else {
                AppUtils.showErrorDialog(context, R.string.edit_dialog_date_end_before_start);

                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean hasToExtendDateStart(final String date) {
        final CacheData cacheData = CacheManager.getInstance().getData();
        int dateStart = cacheData.getStartDate().get(Calendar.HOUR_OF_DAY) * 60 + cacheData.getStartDate().get(Calendar.MINUTE);
        int compareDate = DateUtils.compareDate(date);

        return compareDate < dateStart;
    }

    public static void extendDateStart(final Context context, final String date) {
        final CacheData cacheData = CacheManager.getInstance().getData();
        if (hasToExtendDateStart(date)) {
            String[] tokens = date.split(":");
            if (tokens.length == 2) {
                try {
                    int hour = Integer.parseInt(tokens[0]);
                    int minute = Integer.parseInt(tokens[1]);

                    cacheData.getStartDate().set(Calendar.HOUR_OF_DAY, hour);
                    cacheData.getStartDate().set(Calendar.MINUTE, minute);

                    CacheManager.getInstance().saveCache(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean hasToExtendDateEnd(final String date) {
        final CacheData cacheData = CacheManager.getInstance().getData();
        int dateEnd = cacheData.getEndDate().get(Calendar.HOUR_OF_DAY) * 60 + cacheData.getEndDate().get(Calendar.MINUTE);
        int compareDate = DateUtils.compareDate(date);

        return compareDate > dateEnd;
    }

    public static void extendDateEnd(final Context context, final String date) {
        final CacheData cacheData = CacheManager.getInstance().getData();

        if (hasToExtendDateEnd(date)) {
            String[] tokens = date.split(":");
            if (tokens.length == 2) {
                try {
                    int hour = Integer.parseInt(tokens[0]);
                    int minute = Integer.parseInt(tokens[1]);

                    cacheData.getEndDate().set(Calendar.HOUR_OF_DAY, hour);
                    cacheData.getEndDate().set(Calendar.MINUTE, minute);

                    CacheManager.getInstance().saveCache(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isValidInterpellationDate(Context context, MissionReport report, String date) {
        int compareDate = DateUtils.compareDate(date);

        if (compareDate != -1) {
            int compareDateStart = report.getTimelineDateStart();
            int compareDateEnd = report.getTimelineDateEnd();

            if (compareDate < compareDateStart) {
                AppUtils.showErrorDialog(context, R.string.edit_dialog_date_exists);

                return false;
            }

            if (compareDateEnd != -1 && compareDate > compareDateEnd) {
                AppUtils.showErrorDialog(context, R.string.edit_dialog_date_exists);

                return false;
            }

            return true;
        } else {
            return false;
        }
    }
}
