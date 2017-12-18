package com.ipsis.scan.reporting.edition.model;

import android.util.Log;
import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;

/**
 * Created by pobouteau on 10/3/16.
 */

public class MissionValue implements Serializable {

    private static final long serialVersionUID = 600006L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mFieldId", Integer.class),
            new ObjectStreamField("mLabel", String.class),
            new ObjectStreamField("mValue", String.class)
    };

    private Integer mFieldId;

    private String mLabel;

    private String mValue;

    public MissionValue() {
        super();

        mValue = "";
    }

    public int getFieldId() {
        return mFieldId;
    }

    public void setFieldId(int fieldId) {
        mFieldId = fieldId;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }
}
