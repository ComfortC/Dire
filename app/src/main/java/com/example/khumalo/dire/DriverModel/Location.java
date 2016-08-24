package com.example.khumalo.dire.DriverModel;

/**
 * Created by KHUMALO on 8/24/2016.
 */
public class Location {

    private String latitude;
    private String longitude;

    public Location() {
    }

    public Location(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
