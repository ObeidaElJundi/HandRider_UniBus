package com.coding4fun.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.coding4fun.handrider_unibus.MainMapActivity;
import com.coding4fun.handrider_unibus.MyApp;
import com.coding4fun.handrider_unibus.R;
import com.coding4fun.handrider_unibus.Splash;
import com.coding4fun.models.Passenger;
import com.coding4fun.utils.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by coding4fun on 17-Apr-17.
 */

public class DrivingService extends Service implements LocationListener {

    private MyApp myApp;
    private GoogleApiClient mGoogleApiClient;
    private List<Geofence> mGeofenceList = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(Constants.TAG,"DrivingService started");
        myApp = (MyApp) getApplicationContext();
        myApp.setDrivingServiceRunning(true);
        mGoogleApiClient = myApp.getGoogleApiClient();
        if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null)
            myApp.getUpdateUIonLocationAndGeofenceCallback().onDrivingServiceStarted();
    }

    @Override
    public void onDestroy() {
        myApp.setDrivingServiceRunning(false);
        stopLocationUpdates();
        stopGeofencing();
        myApp.getRide().put("isAvailable",false);
        myApp.getRide().saveInBackground();
        if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null)
            myApp.getUpdateUIonLocationAndGeofenceCallback().onDrivingServiceStopped();
        else myApp.getPassengers().clear();
        if(!myApp.isMapActivityRunning()) mGoogleApiClient.disconnect();
        Log.e(Constants.TAG,"DrivingService stopped");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION_START)) {
            Log.e(Constants.TAG,"DrivingService ACTION_START");
            goForeground();
            startLocationUpdates();
            mGeofenceList.add(buildGeofence(Constants.LOCATION_BAU_BEIRUT, Constants.GEOFENCE_ID_BAU_BEIRUT));
            mGeofenceList.add(buildGeofence(Constants.LOCATION_BAU_DEBBIEH, Constants.GEOFENCE_ID_BAU_DEBBIEH));
            setupGeofences();
        } else if (intent.getAction().equals(Constants.ACTION_STOP)) {
            Log.e(Constants.TAG,"DrivingService ACTION_STOP");
            stopForeground(true);
            stopSelf();
        } else if (intent.getAction().equals(Constants.ACTION_BUS_IS_FULL)) {
            myApp.getRide().put("seats_available",0);
            myApp.getRide().saveInBackground();
            Toast.makeText(this, "Bus is full", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(Constants.ACTION_GEOFENCE_ENTER_BEIRUT)) {
            myApp.setDrivingStatus(Constants.DRIVING_STATUS_WAITING_IN_BEIRUT);
            if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null) myApp.getUpdateUIonLocationAndGeofenceCallback().onEnteringBauBeirut();
            //kill service if driver arrived to destination
            if(intent.hasExtra(Constants.INTENT_EXTRA_FINISH_RIDE_AND_STOP_SERVICE) && intent.getBooleanExtra(Constants.INTENT_EXTRA_FINISH_RIDE_AND_STOP_SERVICE,false)){
                busArrived_NotifyActiveUsers();
                killService();
                destinationArrivedNotification(this,"Beirut");
            }
        } else if (intent.getAction().equals(Constants.ACTION_GEOFENCE_EXIT_BEIRUT)) {
            myApp.setDrivingStatus(Constants.DRIVING_STATUS_GOING_TO_DEBBIEH);
            if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null) myApp.getUpdateUIonLocationAndGeofenceCallback().onExitingBauBeirut();
        } else if (intent.getAction().equals(Constants.ACTION_GEOFENCE_ENTER_DEBBIEH)) {
            myApp.setDrivingStatus(Constants.DRIVING_STATUS_WAITING_IN_DEBBIEH);
            if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null) myApp.getUpdateUIonLocationAndGeofenceCallback().onEnteringBauDebbieh();
            //kill service if driver arrived to destination
            if(intent.hasExtra(Constants.INTENT_EXTRA_FINISH_RIDE_AND_STOP_SERVICE) && intent.getBooleanExtra(Constants.INTENT_EXTRA_FINISH_RIDE_AND_STOP_SERVICE,false)){
                busArrived_NotifyActiveUsers();
                killService();
                destinationArrivedNotification(this,"Debbieh");
            }
        } else if (intent.getAction().equals(Constants.ACTION_GEOFENCE_EXIT_DEBBIEH)) {
            myApp.setDrivingStatus(Constants.DRIVING_STATUS_GOING_TO_BEIRUT);
            if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null) myApp.getUpdateUIonLocationAndGeofenceCallback().onExitingBauDebbieh();
        } else if (intent.getAction().equals(Constants.ACTION_NEW_PASSENGER) && intent.hasExtra("id")) {
            //add geofence & update UI (add marker on passenger location) if map activity is opened
            stopGeofencing();
            Passenger newPassenger = myApp.getPassengers().get(intent.getStringExtra("id"));
            mGeofenceList.add(buildGeofence(newPassenger.getLocation(), newPassenger.getId()));
            setupGeofences();
            if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null) myApp.getUpdateUIonLocationAndGeofenceCallback().onNewPassenger(newPassenger);
        } else if (intent.getAction().equals(Constants.ACTION_CANCEL_PASSENGER) && intent.hasExtra("id")) {
            //remove geofence & update UI (remove marker on passenger location) if map activity is opened
            Passenger passenger = myApp.getPassengers().get(intent.getStringExtra("id"));
            removeOneGeofence(passenger.getId());
            Log.e(Constants.TAG,"cancel passenger >> id: " + passenger.getId() + "   key exists? " + myApp.getPassengers().containsKey(passenger.getId()));
            if(myApp.getPassengers().containsKey(passenger.getId())) myApp.getPassengers().remove(passenger.getId());
            if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null) myApp.getUpdateUIonLocationAndGeofenceCallback().onPassengerCancelled(passenger);
        } else if (intent.getAction().equals(Constants.ACTION_GEOFENCE_ENTER_PASSENGER) && intent.hasExtra("id")){ //remind driver of passenger by notification & change request status
            String id = intent.getStringExtra("id");
            Passenger passenger = myApp.getPassengers().get(id);
            enterPassengerArea_updateRequestStatus(passenger.getRequestID(),myApp.getRide().getObjectId());
            passengerReminderNotification(this,passenger.getName());
            if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null) myApp.getUpdateUIonLocationAndGeofenceCallback().onEnteringPassengerArea(passenger);
        } else if (intent.getAction().equals(Constants.ACTION_GEOFENCE_EXIT_PASSENGER) && intent.hasExtra("id")){ //remove passenger & marker & geofence
            String id = intent.getStringExtra("id");
            Passenger passenger = myApp.getPassengers().get(id);
            myApp.getPassengers().remove(id);
            removeOneGeofence(id);
            if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null) myApp.getUpdateUIonLocationAndGeofenceCallback().onExitingPassengerArea(passenger);
        }
        return START_NOT_STICKY;
    }

    private void killService(){
        stopForeground(true);
        stopSelf();
    }

    private void goForeground() {
        Intent i = new Intent(this, MainMapActivity.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        NotificationCompat.Builder n = new NotificationCompat.Builder(this);
        n.setContentTitle("HandRider");
        n.setContentText("Driving...");
        n.setTicker("Start Driving");
        n.setSmallIcon(R.mipmap.ic_launcher);
        n.setContentIntent(pi);

        Intent i2 = new Intent(this, DrivingService.class);
        i2.setAction(Constants.ACTION_STOP);
        PendingIntent pi2 = PendingIntent.getService(this, 0, i2, 0);
        n.addAction(R.drawable.ic_cancel, "STOP", pi2);

        Intent i3 = new Intent(this, DrivingService.class);
        i3.setAction(Constants.ACTION_BUS_IS_FULL);
        PendingIntent pi3 = PendingIntent.getService(this, 0, i3, 0);
        n.addAction(R.drawable.ic_no_passenger, "FULL", pi3);

        startForeground(911, n.build());
    }

    private void passengerReminderNotification(Context context, String passengerName){
        Intent i = new Intent(context, MainMapActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        NotificationCompat.Builder n = new NotificationCompat.Builder(context);
        n.setContentTitle("Reminder");
        n.setContentText(passengerName + " is within 200 meters...");
        n.setTicker("Reminder");
        n.setDefaults(Notification.DEFAULT_ALL);	//default sound, vibration, light...
        n.setAutoCancel(true);	//to cancel it after clicking it
        n.setSmallIcon(R.drawable.notification_passanger);
        n.setContentIntent(pi);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(334,n.build());
    }

    private void destinationArrivedNotification(Context context, String campus){
        Intent i = new Intent(context, Splash.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        NotificationCompat.Builder n = new NotificationCompat.Builder(context);
        n.setContentTitle("Arrived to " + campus);
        n.setContentText("click to reopen app and start another ride...");
        n.setTicker("Arrived");
        n.setDefaults(Notification.DEFAULT_ALL);	//default sound, vibration, light...
        n.setAutoCancel(true);	//to cancel it after clicking it
        n.setSmallIcon(R.mipmap.ic_launcher);
        n.setContentIntent(pi);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(335,n.build());
    }

    //start listening to location updates if location permission granted
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setSmallestDisplacement(50);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            Log.e(Constants.TAG,"DrivingService startLocationUpdates");
        }
    }

    //stop listening to location updates
    private void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.e(Constants.TAG,"DrivingService stopLocationUpdates");
        }
    }

    /*
    called by google play services api when location is updated.
    1. send the updated location to MainMapActivity via broadcast receiver.
       if broadcast receiver is registered in MainMapActivity, current location marker will be updated on map.
    2. update current location in Parse DB & send current location to active users
    */
    @Override
    public void onLocationChanged(Location location) {
        Log.e(Constants.TAG,"LocationChanged >> "+location.getLatitude() + "," + location.getLongitude());
        /*Intent intent = new Intent(Constants.UPDATE_CURRENT_LOCATION_BROADCAST);
        intent.putExtra(Constants.INTENT_EXTRA_NEW_LAT, location.getLatitude());
        intent.putExtra(Constants.INTENT_EXTRA_NEW_LNG, location.getLongitude());
        intent.putExtra(Constants.INTENT_EXTRA_NEW_BEARING, location.getBearing());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/
        //update ui in map activity if opened
        if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null)
            myApp.getUpdateUIonLocationAndGeofenceCallback().onLocationUpdated(location);
        //update current location in Parse DB
        if(myApp.getRide() != null){
            myApp.getRide().put("current_lat",location.getLatitude());
            myApp.getRide().put("current_lng",location.getLongitude());
            //myApp.getRide().saveInBackground();
            HashMap<String,Object> params = new HashMap<>();
            //params.put("ride_obj",myApp.getRide()); //can not send objects
            params.put("ride_obj_id",myApp.getRide().getObjectId());
            params.put("newLat",location.getLatitude());
            params.put("newLng",location.getLongitude());
            params.put("newBearing",location.getBearing());
            //call cloud function to update current location in Parse DB, notify all active passengers, and get distance & duration left
            ParseCloud.callFunctionInBackground("busDriverCurrentLocationUpdated3", params, new FunctionCallback<String>() {
                @Override
                public void done(String response, ParseException e) {
                    Log.e(Constants.TAG, (e==null) ? "cloud function success >> " + response : "cloud function error >> " + e.getMessage());
                    if(e==null){
                        try{
                            JSONObject jo = new JSONObject(response);
                            if(jo.getString("status").equals("ok")){
                                if(myApp.getUpdateUIonLocationAndGeofenceCallback() != null)
                                    myApp.getUpdateUIonLocationAndGeofenceCallback().onDurationAndDistanceUpdated(jo.getInt("duration"),jo.getInt("distance"));
                            }
                        } catch (Exception ex) {Log.e(Constants.TAG,ex.getMessage());}
                    }
                }
            });
        }
    }

    private void busArrived_NotifyActiveUsers(){
        //call cloud function to notify active users that this bus arrived
        HashMap<String,Object> params = new HashMap<>();
        params.put("ride_obj_id",myApp.getRide().getObjectId());
        Log.e(Constants.TAG,"ride_obj_id = " + myApp.getRide().getObjectId());
        ParseCloud.callFunctionInBackground("busArrived", params, new FunctionCallback<String>() {
            @Override
            public void done(String response, ParseException e) {
                //Log.e(Constants.TAG, (e==null) ? "cloud function success >> " + response : "cloud function error >> " + e.getMessage());
                try{Log.e(Constants.TAG,response);}
                catch (Exception ex){/*who carse*/}
            }
        });
    }

    //call cloud function update request status and to notify that passenger to let him/her update UI
    private void enterPassengerArea_updateRequestStatus(String requestID, String ride_obj_id){
        HashMap<String,Object> params = new HashMap<>();
        params.put("requestID",requestID);
        params.put("ride_obj_id",ride_obj_id);
        Log.e(Constants.TAG,"requestID = " + requestID);
        ParseCloud.callFunctionInBackground("updateRequestStatus", params, new FunctionCallback<String>() {
            @Override
            public void done(String response, ParseException e) {
                //Log.e(Constants.TAG, (e==null) ? "cloud function success >> " + response : "cloud function error >> " + e.getMessage());
                try{Log.e(Constants.TAG,response);}
                catch (Exception ex){/*who carse*/}
            }
        });
    }

    private void setupGeofences() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            /*List<Geofence> mGeofenceList = new ArrayList<>();
            mGeofenceList.add(buildGeofence(Constants.LOCATION_BAU_BEIRUT, Constants.GEOFENCE_ID_BAU_BEIRUT));
            mGeofenceList.add(buildGeofence(Constants.LOCATION_BAU_DEBBIEH, Constants.GEOFENCE_ID_BAU_DEBBIEH));*/
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(mGeofenceList), getGeofencePendingIntent());
            Log.e(Constants.TAG,"DrivingService startGeofencing");
        }
    }

    private Geofence buildGeofence(LatLng point, String id){
        return new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(point.latitude,point.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // I use FLAG_UPDATE_CURRENT so that we get the same pending intent back when starting and stopping geofencing.
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    //stop geofencing service
    private void stopGeofencing() {
        if (mGoogleApiClient != null) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,getGeofencePendingIntent());
            Log.e(Constants.TAG,"DrivingService stopGeofencing");
        }
    }

    //remove one geofence
    private void removeOneGeofence(String geofenceID) {
        if (mGoogleApiClient != null) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, Collections.singletonList(geofenceID));
            Log.e(Constants.TAG,"DrivingService removeOneGeofence ID = " + geofenceID);
        }
    }
}