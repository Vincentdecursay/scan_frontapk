package com.ipsis.scan.reporting.edition.model;

import com.ipsis.scan.utils.SerializableSparseArray;
import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;

/**
 * Created by pobouteau on 9/30/16.
 */

public class MissionType implements Serializable {

    private static final long serialVersionUID = 600005L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mId", Integer.class),
            new ObjectStreamField("mName", String.class),
            new ObjectStreamField("mVersion", Integer.class),
            new ObjectStreamField("mForm", MissionFormData.class),
            new ObjectStreamField("mFields", SerializableSparseArray.class)
    };

    private Integer mId;

    private String mName;

    private Integer mVersion;

    private MissionFormData mForm;

    private SerializableSparseArray<SerializableSparseArray<MissionField>> mFields;

    public MissionType(int id, String name, int version) {
        mId = id;
        mName = name;
        mVersion = version;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        mVersion = version;
    }

    public MissionFormData getForm() {
        return mForm;
    }

    public void setForm(MissionFormData form) {
        mForm = form;
    }

    public SerializableSparseArray<SerializableSparseArray<MissionField>> getFields() {
        return mFields;
    }

    public void setFields(SerializableSparseArray<SerializableSparseArray<MissionField>> fields) {
        mFields = fields;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }
}
