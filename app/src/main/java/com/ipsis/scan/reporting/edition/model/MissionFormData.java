package com.ipsis.scan.reporting.edition.model;

import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;

/**
 * Created by pobouteau on 9/30/16.
 */

public class MissionFormData implements Serializable {

    private static final long serialVersionUID = 600002L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mId", Integer.class),
            new ObjectStreamField("mLocationMode", String.class),
            new ObjectStreamField("mInterpellationEnable", String.class)
    };

    private Integer mId;

    private String mLocationMode;

    private String mInterpellationEnable;

    public MissionFormData(int id, String locationMode, String interpellationEnable) {
        mId = id;
        mLocationMode = locationMode;
        mInterpellationEnable = interpellationEnable;
    }

    public int getId() {
        return mId;
    }

    public String getLocationMode() {
        return mLocationMode;
    }

    public String getInterpellationEnable() {
        return mInterpellationEnable;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }
}
