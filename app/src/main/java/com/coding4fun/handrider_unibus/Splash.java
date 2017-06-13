package com.coding4fun.handrider_unibus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.coding4fun.interfaces.PrepareDataCallbacks;
import com.coding4fun.utils.Utils;
import com.parse.ParseUser;

/**
 * Created by coding4fun on 27-Feb-17.
 */

public class Splash extends AppCompatActivity implements PrepareDataCallbacks {

    private MyApp myApp;
    private ProgressBar pb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        myApp = (MyApp)getApplicationContext();
        myApp.setPrepareDataCallbacks(this);
        pb = (ProgressBar) findViewById(R.id.splash_progress_bar);
        new Timer().execute();
    }

    private boolean loggedIn(){
        return ParseUser.getCurrentUser() != null;
    }

    private void finishSplash(){
        startActivity(new Intent(Splash.this,(!loggedIn()) ? Login.class : MainMapActivity.class));
        Splash.this.finish();
    }

    @Override
    public void start() {
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    public void done() {
        pb.setVisibility(View.GONE);
        finishSplash();
    }

    @Override
    public void error(String error) {
        pb.setVisibility(View.GONE);
        Utils.alertErrorAndExit(this,error);
    }

    private class Timer extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try{Thread.sleep(1000);}
            catch (Exception ignored) {}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(loggedIn()) myApp.prepareData();
            else finishSplash();
        }
    }
}