package com.ipsis.scan.reporting.edition.model;

import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;

/**
 * Created by pobouteau on 9/30/16.
 */

public class MissionField implements Serializable {

    private static final long serialVersionUID = 600001L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mId", Integer.class),
            new ObjectStreamField("mType", String.class),
            new ObjectStreamField("mLabel", String.class),
            new ObjectStreamField("mValue", String.class),
            new ObjectStreamField("mGroup", Integer.class),
            new ObjectStreamField("mOrder", Integer.class),
            new ObjectStreamField("mIcon", String.class)
    };

    private Integer mId;

    private String mType;

    private String mLabel;

    private String mValue;

    private Integer mGroup;

    private Integer mOrder;

    private String mIcon;

    public MissionField() {
        super();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
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

    public int getGroup() {
        return mGroup;
    }

    public void setGroup(int group) {
        mGroup = group;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }

    @Override
    public String toString() {
        return "MissionField{" +
                "mLabel='" + mLabel + '\'' +
                ", mValue='" + mValue + '\'' +
                ", mGroup=" + mGroup +
                ", mOrder=" + mOrder +
                '}';
    }
}
