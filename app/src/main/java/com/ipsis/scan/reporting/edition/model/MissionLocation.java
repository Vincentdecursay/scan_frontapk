package com.ipsis.scan.reporting.edition.model;

import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;

/**
 * Created by pobouteau on 11/22/16.
 */

public class MissionLocation implements Serializable {

    private static final long serialVersionUID = 600007L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mLocation", String.class),
            new ObjectStreamField("mLine", String.class)
    };

    private String mLocation;

    private String mLine;

    public MissionLocation() {
        super();

        mLocation = "";
        mLine = "";
    }

    public MissionLocation(String location) {
        this();

        mLocation = location;
    }

    public MissionLocation(String location, String line) {
        mLocation = location;
        mLine = line;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getLine() {
        return mLine;
    }

    public void setLine(String line) {
        mLine = line;
    }

    @Override
    public String toString() {
        String result = "";

        if (!mLine.isEmpty()) {
            result += mLine + " - ";
        }

        result += mLocation;

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MissionLocation that = (MissionLocation) o;

        if (!mLocation.equals(that.mLocation)) return false;
        return mLine.equals(that.mLine);

    }

    @Override
    public int hashCode() {
        int result = mLocation != null ? mLocation.hashCode() : 0;
        result = 31 * result + (mLine != null ? mLine.hashCode() : 0);
        return result;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }
}
