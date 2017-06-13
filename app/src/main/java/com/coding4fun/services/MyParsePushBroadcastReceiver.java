package com.coding4fun.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.coding4fun.handrider_unibus.MainMapActivity;
import com.coding4fun.handrider_unibus.MyApp;
import com.coding4fun.handrider_unibus.R;
import com.coding4fun.models.Passenger;
import com.coding4fun.utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

/**
 * Created by coding4fun on 23-Apr-17.
 */

public class MyParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        //super.onPushReceive(context, intent);

        if(intent == null) return;
        MyApp myApp = ((MyApp)context.getApplicationContext());

        try{
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.e(Constants.TAG,"onPushReceive: " + json);
            String type = json.getString("type");
            if(type.equals("passenger")){
                if(myApp == null || !myApp.isDrivingServiceRunning()) return;
                //get passenger location (lat & lng) and details
                double lat = json.getDouble("lat");
                double lng = json.getDouble("lng");
                String id = json.getString("id");
                String requestID = json.getString("request_id");
                String name = json.getString("name");

                //add geofence on passenger location & update UI (add marker on passenger location) if map activity is opened
                Intent i2 = new Intent(context, DrivingService.class);
                i2.putExtra("id",id);

                if(json.getString("status").equals("CANCELLED")){
                    //for(String key : myApp.getPassengers().keySet()) Log.e(Constants.TAG,"passenger key >> " + key);
                    //if(myApp.getPassengers().containsKey(id)) myApp.getPassengers().remove(id);
                    if(!myApp.getPassengers().containsKey(id)) {
                        //Log.e(Constants.TAG,"no key !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! >> " + id);
                        return;
                    }
                    i2.setAction(Constants.ACTION_CANCEL_PASSENGER);
                } else {
                    //add new passenger to list
                    myApp.getPassengers().put(id,new Passenger(id,requestID,name,new LatLng(lat,lng)));
                    i2.setAction(Constants.ACTION_NEW_PASSENGER);
                }

                //i2.putExtra("lat",lat);
                //i2.putExtra("lng",lng);
                context.startService(i2);
                //notify driver
                if(!json.getString("status").equals("CANCELLED")) newPassengerNotification(context);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG,"ParsePushBroadcastReceiver Error: " + e.getMessage());
        }

    }

    private void newPassengerNotification(Context context){
        Intent i = new Intent(context, MainMapActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        NotificationCompat.Builder n = new NotificationCompat.Builder(context);
        n.setContentTitle("HandRider");
        n.setContentText("New passenger...");
        n.setTicker("New passenger");
        n.setDefaults(Notification.DEFAULT_ALL);	//default sound, vibration, light...
        n.setAutoCancel(true);	//to cancel it after clicking it
        n.setSmallIcon(R.drawable.notification_passanger);
        n.setContentIntent(pi);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(333,n.build());
    }

}