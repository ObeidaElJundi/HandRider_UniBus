package com.coding4fun.interfaces;

import android.location.Location;

import com.coding4fun.models.Passenger;

/**
 * Created by coding4fun on 18-Apr-17.
 */

public interface UpdateUIonLocationAndGeofenceCallback {

    void onLocationUpdated(Location location);
    void onDurationAndDistanceUpdated(int duration, int distance);
    void onDrivingServiceStarted();
    void onDrivingServiceStopped();
    void onEnteringBauBeirut();
    void onExitingBauBeirut();
    void onEnteringBauDebbieh();
    void onExitingBauDebbieh();
    void onNewPassenger(Passenger passenger);
    void onPassengerCancelled(Passenger passenger);
    void onEnteringPassengerArea(Passenger passenger);
    void onExitingPassengerArea(Passenger passenger);
}