package com.coding4fun.handrider_unibus;

import android.app.Application;
import android.util.Log;

import com.coding4fun.interfaces.PrepareDataCallbacks;
import com.coding4fun.interfaces.UpdateUIonLocationAndGeofenceCallback;
import com.coding4fun.models.Passenger;
import com.coding4fun.utils.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by coding4fun on 17-Apr-17.
 */

public class MyApp extends Application {

    private boolean isDrivingServiceRunning = false, isMapActivityRunning = false;
    private String drivingStatus;
    private GoogleApiClient googleApiClient;
    private UpdateUIonLocationAndGeofenceCallback updateUIonLocationAndGeofenceCallback; //to update activity UI from services
    private PrepareDataCallbacks prepareDataCallbacks;
    private ParseObject driver,vehicle,ride;
    private List<ParseObject> universities = new ArrayList<>();
    private Map<String,Passenger> passengers = new HashMap<>(); //key is user id

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(Constants.TAG,"App started");
        initParse();
    }

    private void initParse(){
        //Parse.addParseNetworkInterceptor(new ParseLogInterceptor());
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getResources().getString(R.string.parse_application_id))
                .clientKey(getResources().getString(R.string.parse_client_key))
                .server(getResources().getString(R.string.parse_server))
                .build());
        ParseFacebookUtils.initialize(this);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public boolean isMapActivityRunning() {
        return isMapActivityRunning;
    }

    public void setMapActivityRunning(boolean mapActivityRunning) {
        isMapActivityRunning = mapActivityRunning;
    }

    public boolean isDrivingServiceRunning() {
        return isDrivingServiceRunning;
    }

    public void setDrivingServiceRunning(boolean drivingServiceRunning) {
        isDrivingServiceRunning = drivingServiceRunning;
    }

    public String getDrivingStatus() {
        return drivingStatus;
    }

    public void setDrivingStatus(String drivingStatus) {
        this.drivingStatus = drivingStatus;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public UpdateUIonLocationAndGeofenceCallback getUpdateUIonLocationAndGeofenceCallback() {
        return updateUIonLocationAndGeofenceCallback;
    }

    public void setUpdateUIonLocationAndGeofenceCallback(UpdateUIonLocationAndGeofenceCallback updateUIonLocationAndGeofenceCallback) {
        this.updateUIonLocationAndGeofenceCallback = updateUIonLocationAndGeofenceCallback;
    }

    public PrepareDataCallbacks getPrepareDataCallbacks() {
        return prepareDataCallbacks;
    }

    public void setPrepareDataCallbacks(PrepareDataCallbacks prepareDataCallbacks) {
        this.prepareDataCallbacks = prepareDataCallbacks;
    }

    public ParseObject getDriver() {
        return driver;
    }

    public void setDriver(ParseObject driver) {
        this.driver = driver;
    }

    public ParseObject getVehicle() {
        return vehicle;
    }

    public void setVehicle(ParseObject vehicle) {
        this.vehicle = vehicle;
    }

    public ParseObject getRide() {
        return ride;
    }

    public void setRide(ParseObject ride) {
        this.ride = ride;
    }

    public List<ParseObject> getUniversities() {
        return universities;
    }

    public void setUniversities(List<ParseObject> universities) {
        this.universities = universities;
    }

    public Map<String, Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(Map<String, Passenger> passengers) {
        this.passengers = passengers;
    }

    public void prepareData(){
        if(prepareDataCallbacks != null) prepareDataCallbacks.start();
        getUniversitiesData();
    }

    private void getUniversitiesData(){
        ParseQuery<ParseObject> drivers = ParseQuery.getQuery("University_Location");
        drivers.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    for(ParseObject po : objects) universities.add(po);
                    getDriverAndVehicleData();
                } else {
                    if(prepareDataCallbacks != null) prepareDataCallbacks.error(e.getMessage());
                }
            }
        });
    }

    private void getDriverAndVehicleData(){
        final ParseQuery<ParseObject> drivers = ParseQuery.getQuery("Driver");
        drivers.whereEqualTo("user_obj", ParseUser.getCurrentUser());
        drivers.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    driver = objects.get(0);
                    vehicle = driver.getParseObject("vehicle_obj");
                    if(prepareDataCallbacks != null) prepareDataCallbacks.done();
                } else {
                    if(prepareDataCallbacks != null) prepareDataCallbacks.error(e.getMessage());
                }
            }
        });
    }
}