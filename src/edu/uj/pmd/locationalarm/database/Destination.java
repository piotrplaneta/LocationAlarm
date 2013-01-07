package edu.uj.pmd.locationalarm.database;

/**
 * User: piotrplaneta
 * Date: 29.12.2012
 * Time: 16:04
 */
public class Destination {
    private int id;
    private String name;
    private double longitude;
    private double latitude;

    public Destination() {
    }

    public Destination(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Destination(int id, String name, double longitude, double latitude) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
