package com.coding4fun.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.coding4fun.handrider_unibus.MyApp;
import com.coding4fun.handrider_unibus.R;
import com.coding4fun.utils.Constants;

/**
 * Created by coding4fun on 17-Apr-17.
 */

public class TestForegroundServiceActivity extends AppCompatActivity {

    MyApp myApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_foreground_service_activity);
        myApp = (MyApp)getApplicationContext();
        //myApp.setTest(999);
    }

    public void startService(View v) {
        Intent i = new Intent(this,TestForegroundService.class);
        i.setAction(Constants.ACTION_START);
        startService(i);
    }

    @Override
    protected void onDestroy() {
        //myApp.setTest(55);
        super.onDestroy();
    }
}