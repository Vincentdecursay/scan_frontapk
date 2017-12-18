package com.ipsis.scan.database.model;

import android.util.Log;
import com.ipsis.scan.utils.SerializationUtils;
import com.ipsis.scan.utils.StringUtils;

import java.io.*;

/**
 * Created by etude on 15/10/15.
 */
public class Route implements Serializable {

    private static final long serialVersionUID = 200001L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("routeId", String.class),
            new ObjectStreamField("shortName", String.class),
            new ObjectStreamField("longName", String.class),
            new ObjectStreamField("type", Type.class),
            new ObjectStreamField("fromName", String.class),
            new ObjectStreamField("toName", String.class),
            new ObjectStreamField("direction", String.class)
    };

    public enum Type {
        RATP, TRANSILIEN, OPTILE, STIF
    }

    private String routeId;
    private String shortName;
    private String longName;
    private Type type;

    private String fromName;
    private String toName;
    private String direction;

    private Route(String routeId, String shortName, String longName) {
        this.routeId = routeId;
        this.shortName = shortName;
        this.longName = longName;

        this.fromName = "";
        this.toName = "";
        this.direction = "";

        //this.longName = this.longName.replaceAll("\\.\\)", "");
        //this.longName = this.longName.replaceAll("\\.$", "");
    }

    public static Route fromRatp(String routeId, String shortName, String longName) {
        Route route = new Route(routeId, shortName, longName);

        if (route.longName.indexOf("<") > 0) {
            route.fromName = route.longName.substring(route.longName.indexOf("(") + 1, route.longName.indexOf("<") - 1);
            route.toName = route.longName.substring(route.longName.indexOf(">") + 2, route.longName.lastIndexOf(")"));

            if (route.fromName.indexOf("(") == 0) {
                route.fromName = route.fromName.substring(1, route.fromName.lastIndexOf(")"));
            }

            if (route.toName.indexOf("(") == 0) {
                route.toName = route.toName.substring(1, route.toName.lastIndexOf(")"));
            }
        } else {
            route.fromName = route.longName.substring(route.longName.indexOf("(") + 1, route.longName.indexOf(")"));
        }
        route.direction = route.longName.substring(route.longName.lastIndexOf("-") + 2);

        route.fromName = StringUtils.capitalizeLineName(route.fromName);

        if (route.toName != null) {
            route.toName = StringUtils.capitalizeLineName(route.toName);
        }

        route.type = Type.RATP;

        return route;
    }

    public static Route fromTransilien(String routeId, String shortName, String longName) {
        Route route = new Route(routeId, shortName, longName);

        int separatorIndex = route.longName.indexOf(" - ");
        if (separatorIndex > 0) {
            route.fromName = route.longName.substring(0, separatorIndex);
            route.toName = route.longName.substring(separatorIndex + 3, route.longName.length());
        }

        route.fromName = StringUtils.capitalizeLineName(route.fromName);

        if (route.toName != null) {
            route.toName = StringUtils.capitalizeLineName(route.toName);
        }

        route.type = Type.TRANSILIEN;

        return route;
    }

    public static Route fromOptile(String routeId, String shortName, String longName) {
        Route route = new Route(routeId, shortName, longName);

        /*int separatorIndex = route.longName.indexOf(" - ");
        if (separatorIndex > 0) {
            route.fromName = route.longName.substring(0, separatorIndex);
            route.toName = route.longName.substring(separatorIndex + 3, route.longName.length());
        }*/
        route.longName = StringUtils.capitalizeLineName(route.longName);

        route.type = Type.OPTILE;

        return route;
    }

    public static Route fromStif(String routeId, String shortName, String longName) {
        Route route = new Route(routeId, shortName, longName);

        /*int separatorIndex = route.longName.indexOf(" - ");
        if (separatorIndex > 0) {
            route.fromName = route.longName.substring(0, separatorIndex);
            route.toName = route.longName.substring(separatorIndex + 3, route.longName.length());
        }*/
        //route.longName = StringUtils.capitalizeLineName(route.longName);

        route.type = Type.STIF;

        return route;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getShortName() {
        return shortName;
    }

    /*public String getLongName() {
        if (this.toName == null) {
            return this.fromName + " (" + this.direction + ")";
        } else {
            return this.fromName + " -> " + this.toName + " (" + this.direction + ")";
        }
    }*/
    public String getLongName() {
        // String result = this.shortName + " " + this.fromName;
        /*String result = this.fromName;

        if (this.toName != null) {
            result += " / " + this.toName;
        }*/
        String result = this.longName;

        if (!this.fromName.isEmpty()) {
            result = this.fromName;

            if (this.toName != null) {
                result += " / " + this.toName;
            }
        }

        return result;
    }

    public int getLineNumber() {
        try {
            return Integer.parseInt(shortName);
        } catch (Exception e) {
            return -1;
        }
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        SerializationUtils.readObject(this, input, serialPersistentFields);
    }

    private void writeObject(ObjectOutputStream output) throws IOException {
        SerializationUtils.writeObject(this, output, serialPersistentFields);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Route route = (Route) o;

        String longName = getLongName();

        //if (routeId != null ? !routeId.equals(route.routeId) : route.routeId != null) return false;
        //if (shortName != null ? !shortName.equals(route.shortName) : route.shortName != null) return false;
        if (longName != null ? !longName.equals(route.getLongName()) : route.getLongName() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = routeId != null ? routeId.hashCode() : 0;
        result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
        result = 31 * result + (longName != null ? longName.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Route{" +
                "shortName='" + shortName + '\'' +
                ", fromName='" + fromName + '\'' +
                ", toName='" + toName + '\'' +
                ", direction='" + direction + '\'' +
                '}';
    }
}
