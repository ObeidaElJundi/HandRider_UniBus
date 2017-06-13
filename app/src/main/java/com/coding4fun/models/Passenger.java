package com.coding4fun.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by coding4fun on 23-Apr-17.
 */

public class Passenger {

    private String name, id, requestID;
    private LatLng location;

    public Passenger(String id, String requestID, String name, LatLng location) {
        this.id = id;
        this.requestID = requestID;
        this.name = name;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}