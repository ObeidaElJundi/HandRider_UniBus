package com.coding4fun.test;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.coding4fun.handrider_unibus.MyApp;
import com.coding4fun.handrider_unibus.R;
import com.coding4fun.utils.Constants;


/**
 * Created by coding4fun on 17-Apr-17.
 */

public class TestForegroundService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(Constants.TAG,"service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(Constants.TAG,"service onStartCommand");
        if(intent.getAction().equals(Constants.ACTION_START)){
            Log.e(Constants.TAG,"service ACTION_START");
            foreground();
            new Whatever().execute();
        } else if(intent.getAction().equals(Constants.ACTION_STOP)){
            Log.e(Constants.TAG,"service ACTION_STOP");
            stopForeground(true);
            stopSelf();
        }
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(Constants.TAG,"service destroyed");
        super.onDestroy();
    }

    private void foreground(){
        Intent i = new Intent(this,TestForegroundServiceActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,i,0);
        NotificationCompat.Builder n = new NotificationCompat.Builder(this);
        n.setContentTitle("foreground service");
        n.setContentText("testing...");
        n.setTicker("whatever");
        n.setSmallIcon(R.mipmap.ic_launcher);
        n.setContentIntent(pi);

        Intent i2 = new Intent(this,TestForegroundService.class);
        i2.setAction(Constants.ACTION_STOP);
        PendingIntent pi2 = PendingIntent.getService(this,0,i2,0);
        n.addAction(R.mipmap.ic_launcher,"STOP",pi2);

        startForeground(911,n.build());
    }

    private class Whatever extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for(int i=0;i<10;i++){
                try{Thread.sleep(1000);}
                catch (Exception ex) {}
                MyApp myApp = (MyApp)getApplicationContext();
                Log.e(Constants.TAG, (myApp != null) ? "MyApp is NOT null :)" : "MyApp is null!");
                //Log.e(Constants.TAG, (myApp != null) ? "test="+myApp.getTest() : "MyApp is null!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            stopForeground(true);
            stopSelf();
        }
    }
}