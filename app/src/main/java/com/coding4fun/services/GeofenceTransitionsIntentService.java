package com.coding4fun.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.coding4fun.handrider_unibus.MyApp;
import com.coding4fun.utils.Constants;
import com.coding4fun.utils.Utils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by coding4fun on 17-Apr-17.
 */

public class GeofenceTransitionsIntentService extends IntentService {


    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        MyApp myApp = (MyApp) getApplicationContext();
        Intent drivingService = new Intent(this, DrivingService.class);

        //check if there is any error. If any, terminate
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Get the geofences that were triggered. A single event can trigger multiple geofences.
        // check http://stackoverflow.com/questions/43061009/why-geoevent-returns-a-list-instead-of-single-geofence
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        Log.e(Constants.TAG, "triggeringGeofences size = " + triggeringGeofences.size());
        if(triggeringGeofences.size() > 1) return;
        if(myApp == null || myApp.getRide() == null) return;

        //get both universities parse object
        ParseObject BAU_Beirut=null, BAU_Debbieh=null;
        for(ParseObject po : myApp.getUniversities()){
            if(po.getString("campus").toLowerCase().equals("beirut")) BAU_Beirut = po;
            else if(po.getString("campus").toLowerCase().equals("debbieh")) BAU_Debbieh = po;
        }

        //check who triggered geofence : BAU Beirut || BAU Debbieh
        if(triggeringGeofences.get(0).getRequestId().equals(Constants.GEOFENCE_ID_BAU_BEIRUT)){
            //start_campus=beirutUni; destination_campus=debbiehUni
            ParseObject prev_destination_university_obj = (ParseObject) myApp.getRide().get("destination_university_obj");
            myApp.getRide().put("source_university_obj",BAU_Beirut);
            myApp.getRide().put("destination_university_obj",BAU_Debbieh);
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.e(Constants.TAG,"entered BAU Beirut");
                drivingService.setAction(Constants.ACTION_GEOFENCE_ENTER_BEIRUT);
                if(myApp.getRide().getBoolean("isMoving") && myApp.getRide().getBoolean("isAvailable") && prev_destination_university_obj==BAU_Beirut){
                    //driver arrived to destination >> finish ride & stop service
                    Log.e(Constants.TAG, "arrived BAU Beirut");
                    myApp.getRide().put("isMoving", false);
                    myApp.getRide().put("isAvailable",false);
                    myApp.getRide().put("travel_end_time", Utils.getCurrentDateAndTime());
                    drivingService.putExtra(Constants.INTENT_EXTRA_FINISH_RIDE_AND_STOP_SERVICE,true);
                }
            } else {
                Log.e(Constants.TAG,"exited BAU Beirut");
                drivingService.setAction(Constants.ACTION_GEOFENCE_EXIT_BEIRUT);
                myApp.getRide().put("isMoving",true);
            }
            myApp.getRide().saveInBackground();
        } else if(triggeringGeofences.get(0).getRequestId().equals(Constants.GEOFENCE_ID_BAU_DEBBIEH)) {
            //start_campus=debbiehUni; destination_campus=beirutUni
            ParseObject prev_destination_university_obj = (ParseObject) myApp.getRide().get("destination_university_obj");
            myApp.getRide().put("source_university_obj", BAU_Debbieh);
            myApp.getRide().put("destination_university_obj", BAU_Beirut);
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.e(Constants.TAG, "entered BAU Debbieh");
                drivingService.setAction(Constants.ACTION_GEOFENCE_ENTER_DEBBIEH);
                if(myApp.getRide().getBoolean("isMoving") && myApp.getRide().getBoolean("isAvailable") && prev_destination_university_obj==BAU_Debbieh){
                    //driver arrived to destination >> finish ride & stop service
                    Log.e(Constants.TAG, "arrived BAU Debbieh");
                    myApp.getRide().put("isMoving", false);
                    myApp.getRide().put("isAvailable",false);
                    myApp.getRide().put("travel_end_time", Utils.getCurrentDateAndTime());
                    drivingService.putExtra(Constants.INTENT_EXTRA_FINISH_RIDE_AND_STOP_SERVICE,true);
                }
            } else {
                Log.e(Constants.TAG, "exited BAU Debbieh");
                drivingService.setAction(Constants.ACTION_GEOFENCE_EXIT_DEBBIEH);
                myApp.getRide().put("isMoving", true);
            }
            myApp.getRide().saveInBackground();
        //} else if(triggeringGeofences.get(0).getRequestId().equals(Constants.GEOFENCE_ID_PASSENGER)){
        } else { //passenger
            String id = triggeringGeofences.get(0).getRequestId();
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.e(Constants.TAG,"entered Passenger area");
                drivingService.setAction(Constants.ACTION_GEOFENCE_ENTER_PASSENGER);
                drivingService.putExtra("id",id);
            } else {
                Log.e(Constants.TAG,"exited Passenger area");
                drivingService.setAction(Constants.ACTION_GEOFENCE_EXIT_PASSENGER);
                drivingService.putExtra("id",id);
            }
        }

        startService(drivingService);

    }
}