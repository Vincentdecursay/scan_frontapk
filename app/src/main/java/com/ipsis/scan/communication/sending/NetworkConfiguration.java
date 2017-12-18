package com.ipsis.scan.communication.sending;

import android.util.Log;
import com.ipsis.scan.utils.SerializationUtils;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Created by pobouteau on 10/26/16.
 */

public class NetworkConfiguration implements Serializable {

    private static final long serialVersionUID = 100001L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("mUsername", String.class),
            new ObjectStreamField("mApiToken", String.class)
    };

    /**
     * Username
     */
    private String mUsername;

    /**
     * Api token
     */
    private String mApiToken;

    public NetworkConfiguration() {
        super();
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getApiToken() {
        return mApiToken;
    }

    public void setApiToken(String apiToken) {
        mApiToken = apiToken;
    }

    public NetworkConfiguration copy() {
        NetworkConfiguration networkConfiguration = new NetworkConfiguration();

        networkConfiguration.setUsername(getUsername());
        networkConfiguration.setApiToken(getApiToken());

        return networkConfiguration;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }

    @Override
    public String toString() {
        return "NetworkConfiguration{" +
                "mUsername='" + mUsername + '\'' +
                ", mApiToken='" + mApiToken + '\'' +
                '}';
    }
}
