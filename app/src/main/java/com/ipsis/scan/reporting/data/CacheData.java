package com.ipsis.scan.reporting.data;

import com.ipsis.scan.communication.sending.NetworkConfiguration;
import com.ipsis.scan.reporting.activities.search.SearchLocationHistory;
import com.ipsis.scan.reporting.edition.MissionForms;
import com.ipsis.scan.reporting.edition.model.MissionReport;
import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pobouteau on 9/29/16.
 */

public class CacheData implements Serializable {

    private static final long serialVersionUID = 400001L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mCacheVersion", Integer.class),
            new ObjectStreamField("mNetworkConfiguration", NetworkConfiguration.class),
            new ObjectStreamField("mDate", Calendar.class),
            new ObjectStreamField("mStartDate", Calendar.class),
            new ObjectStreamField("mEndDate", Calendar.class),
            new ObjectStreamField("mMissionReports", ArrayList.class),
            new ObjectStreamField("mMissionForms", MissionForms.class),
            new ObjectStreamField("mLocationHistory", ArrayList.class),
            new ObjectStreamField("mCommentEmail", String.class),
            new ObjectStreamField("mAddress", String.class)
    };

    /**
     * Version of the cache
     */
    public static final int VERSION = 81;

    /**
     * Version of the loaded cache
     */
    private Integer mCacheVersion;

    private NetworkConfiguration mNetworkConfiguration;

    /**
     * Date of missions
     */
    private Calendar mDate;

    /**
     * Starting hour
     */
    private Calendar mStartDate;

    /**
     * Ending hour
     */
    private Calendar mEndDate;

    /**
     * Reports
     */
    private ArrayList<MissionReport> mMissionReports;

    /**
     * Forms struture
     */
    private MissionForms mMissionForms;

    /**
     * Locations history
     */
    private ArrayList<SearchLocationHistory> mLocationHistory;

    private String mCommentEmail;

    private String mAddress;

    /**
     * Default constructor
     */
    public CacheData() {
        super();

        mCacheVersion = VERSION;

        mLocationHistory = new ArrayList<>();

        mCommentEmail = "isabellem.martinez@interieur.gouv.fr";
        mAddress = "BRF\n54 Quai de la Rapée\n75012 Paris";
    }

    public void initDay() {
        mDate = Calendar.getInstance();

        mStartDate = Calendar.getInstance();
        mStartDate.set(Calendar.HOUR_OF_DAY, 8);
        mStartDate.set(Calendar.MINUTE, 0);

        mEndDate = Calendar.getInstance();
        mEndDate.set(Calendar.HOUR_OF_DAY, 18);
        mEndDate.set(Calendar.MINUTE, 0);

        mMissionReports = new ArrayList<>();
    }

    public int getCacheVersion() {
        return mCacheVersion;
    }

    public NetworkConfiguration getNetworkConfiguration() {
        return mNetworkConfiguration;
    }

    public void setNetworkConfiguration(NetworkConfiguration networkConfiguration) {
        mNetworkConfiguration = networkConfiguration.copy();
    }

    public Calendar getDate() {
        return mDate;
    }

    public void setDate(Calendar date) {
        mDate = date;
    }

    public Calendar getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Calendar startDate) {
        mStartDate = startDate;
    }

    public Calendar getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Calendar endDate) {
        mEndDate = endDate;
    }

    public MissionForms getMissionForms() {
        return mMissionForms;
    }

    public ArrayList<MissionReport> getMissionReports() {
        return mMissionReports;
    }

    public void setMissionForms(MissionForms missionForms) {
        mMissionForms = missionForms;
    }

    public ArrayList<SearchLocationHistory> getLocationHistory() {
        return mLocationHistory;
    }

    public void addLocationHistory(SearchLocationHistory location) {
        if (!mLocationHistory.contains(location)) {
            mLocationHistory.add(0, location);

            if (mLocationHistory.size() > 10) {
                mLocationHistory.remove(mLocationHistory.size() - 1);
            }
        }
    }

    public String getCommentEmail() {
        return mCommentEmail;
    }

    public void setCommentEmail(String commentEmail) {
        mCommentEmail = commentEmail;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public boolean isSynced() {
        for (MissionReport report : mMissionReports) {
            if (!report.isSync()) {
                return false;
            }
        }

        return mMissionReports.size() > 0;
    }

    public boolean isSyncing() {
        for (MissionReport report : mMissionReports) {
            if (report.isSyncing()) {
                return true;
            }
        }

        return false;
    }

    public void resetSync() {
        for (MissionReport report : mMissionReports) {
            report.setSync(false);
        }
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }

    @Override
    public String toString() {
        return "CacheData{" +
                "mCacheVersion=" + mCacheVersion +
                ", mDate=" + mDate +
                ", mStartDate=" + mStartDate +
                ", mEndDate=" + mEndDate +
                '}';
    }
}
