package com.ipsis.scan.database.model;

import com.ipsis.scan.utils.SerializationUtils;
import com.ipsis.scan.utils.StringUtils;

import java.io.*;

/**
 * Created by etude on 16/10/15.
 */
public class Stop implements Serializable {

    private static final long serialVersionUID = 200002L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("name", String.class),
            new ObjectStreamField("desc", String.class),
            new ObjectStreamField("latitude", Double.class),
            new ObjectStreamField("longitude", Double.class)
    };

    private String name;
    private String desc;
    private Double latitude;
    private Double longitude;

    public Stop(String name, String desc, double latitude, double longitude) {
        this.name = StringUtils.capitalizeLineName(name);
        this.desc = desc;
        this.latitude = latitude;
        this.longitude = longitude;

        this.name = this.name.replaceFirst("\\.$", "");
    }

    public String getName() {
        return name;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }

    @Override
    public String toString() {
        return "Stop{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
