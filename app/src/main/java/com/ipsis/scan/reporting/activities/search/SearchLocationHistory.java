package com.ipsis.scan.reporting.activities.search;

import com.ipsis.scan.reporting.edition.model.MissionLocation;
import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;

/**
 * Created by pobouteau on 10/17/16.
 */

public class SearchLocationHistory implements Serializable {

    private static final long serialVersionUID = 300001L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mType", Integer.class),
            new ObjectStreamField("mTitle", MissionLocation.class),
            new ObjectStreamField("mTag", Object.class)
    };

    private Integer mType;

    private MissionLocation mTitle;

    private Object mTag;

    public SearchLocationHistory(int type, MissionLocation title, Object tag) {
        mType = type;
        mTitle = title;
        mTag = tag;
    }

    public int getType() {
        return mType;
    }

    public MissionLocation getTitle() {
        return mTitle;
    }

    public Object getTag() {
        return mTag;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }

    @Override
    public String toString() {
        return "SearchLocationHistory{" +
                "mType=" + mType +
                ", mTitle='" + mTitle + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchLocationHistory that = (SearchLocationHistory) o;

        if (mType != that.mType) return false;
        return mTitle.equals(that.mTitle);
    }

    @Override
    public int hashCode() {
        int result = mType;
        result = 31 * result + mTitle.hashCode();
        return result;
    }
}
