package com.ipsis.scan.utils;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Created by pobouteau on 10/27/16.
 */

public class SerializationUtils {
    public static <T> T deepClone(T object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void readObject(Object object, ObjectInputStream input, ObjectStreamField[] serialPersistentFields) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField reader = input.readFields();
        for (ObjectStreamField field : serialPersistentFields) {
            try {
                Field classField = object.getClass().getDeclaredField(field.getName());
                boolean accessible = classField.isAccessible();
                if (!accessible) {
                    classField.setAccessible(true);
                }

                classField.set(object, reader.get(field.getName(), classField.get(object)));

                if (!accessible) {
                    classField.setAccessible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeObject(Object object, ObjectOutputStream output, ObjectStreamField[] serialPersistentFields) throws IOException {
        ObjectOutputStream.PutField writer = output.putFields();

        for (ObjectStreamField field : serialPersistentFields) {
            try {
                Field classField = object.getClass().getDeclaredField(field.getName());
                boolean accessible = classField.isAccessible();
                if (!accessible) {
                    classField.setAccessible(true);
                }

                writer.put(field.getName(), classField.get(object));

                if (!accessible) {
                    classField.setAccessible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        output.writeFields();
    }
}
