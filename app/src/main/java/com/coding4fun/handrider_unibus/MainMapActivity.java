package com.coding4fun.handrider_unibus;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.coding4fun.interfaces.UpdateUIonLocationAndGeofenceCallback;
import com.coding4fun.models.Passenger;
import com.coding4fun.services.DrivingService;
import com.coding4fun.utils.Constants;
import com.coding4fun.utils.SpeechRecognizerManager;
import com.coding4fun.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by coding4fun on 17-Apr-17.
 */

public class MainMapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, UpdateUIonLocationAndGeofenceCallback {

    private final int REQUEST_GOOGLE_PLAY_SERVICES = 44;
    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 64;
    private final int REQUEST_PERMISSION_RECORD_AUDIO = 66;
    private final int REQUEST_CHECK_SETTINGS = 88;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Map<String,Marker> passengersMarkers = new HashMap<>();

    private void checkGoogleApiAvailabilityAndGetGoogleApiClient(){
        GoogleApiAvailability gAPI = GoogleApiAvailability.getInstance();
        int isAvailable = gAPI.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) { //everything is OK
            initGoogleApiClient();
        } else if (gAPI.isUserResolvableError(isAvailable)) { //there is an error, but the user can do something about it
            gAPI.showErrorDialogFragment(this, isAvailable, REQUEST_GOOGLE_PLAY_SERVICES);
        } else { //there is an error, and the user can't do something about it...
            Utils.alertErrorAndExit(this,"Google Play Services are NOT available!");
        }
    }

    //initialize Google Api Client
    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myApp.setGoogleApiClient(mGoogleApiClient);
        checkLocationPermissionsAndSettings();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void checkLocationPermissionsAndSettings(){
        //check if FINE location permission is granted. If not, request...
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            return;
        }
        //check if location setting is turned on. If not, ask user to turn it on
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                //final LocationSettingsStates codes = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS: //location is ON
                        initMap();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: //location is OFF. Trying to turn on...
                        try {
                            status.startResolutionForResult(MainMapActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException ignored) {}
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: //location is OFF (Unavailable)
                        Utils.alertErrorAndExit(MainMapActivity.this,"Location Settings issue!");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //initialize google map
    private void initMap() {
        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mf.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMapListener();
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //add 2 bau markers and scale map to show them
                mMap.addMarker(new MarkerOptions().position(Constants.LOCATION_BAU_BEIRUT).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bau_logo)));
                mMap.addMarker(new MarkerOptions().position(Constants.LOCATION_BAU_DEBBIEH).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bau_logo)));
                scale(Constants.LOCATION_BAU_BEIRUT,Constants.LOCATION_BAU_DEBBIEH);
            }
        });
        //register UI updates callbacks here. otherwise, it may cause errors if ui is tried to be updated before map is ready
        myApp.setUpdateUIonLocationAndGeofenceCallback(this);
        if(myApp.isDrivingServiceRunning()) appOpenedWhileDrivingServiceIsOn_updateUI();
    }

    private void setMapListener(){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.e(Constants.TAG,"clicked >> lat: "+latLng.latitude+" , lng: "+latLng.longitude);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_CHECK_SETTINGS:
                if(resultCode == RESULT_OK) initMap();
                else Utils.alertErrorAndExit(this,"Location Settings issue!");
                break;

            case REQUEST_GOOGLE_PLAY_SERVICES:
                if(resultCode == RESULT_OK) initGoogleApiClient();
                else Utils.alertErrorAndExit(this,"Google Play Services are NOT available!");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_ACCESS_FINE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkLocationPermissionsAndSettings();
                } else {
                    //Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    Utils.alertErrorAndExit(this,"Permission Denied!");
                }
                break;
            case REQUEST_PERMISSION_RECORD_AUDIO:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setSpeechListener();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /*********************************************************************/

    private MyApp myApp;
    private Marker currentLocationMarker;
    private TextView drivingDetailtsTV,startDrivingDialogTitleTV,distanceTV,durationTV;
    private FloatingActionButton startDrivingFAB;
    private Button letsGO;
    private ProgressDialog mProgressDialog;
    private AlertDialog startDrivingTimeDialog;
    private AppCompatRadioButton now,after,at;
    private WheelPicker after_hours,after_minutes,at_hour,at_minute,at_am_pm;
    private boolean trackMe = false;

    /*private BroadcastReceiver updateCurrentLocationBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get current lat & lng & bearing & update marker
            double lat = intent.getDoubleExtra(Constants.INTENT_EXTRA_NEW_LAT,(currentLocation!=null)?currentLocation.latitude:0d);
            double lng = intent.getDoubleExtra(Constants.INTENT_EXTRA_NEW_LNG,(currentLocation!=null)?currentLocation.longitude:0d);
            float bearing = intent.getFloatExtra(Constants.INTENT_EXTRA_NEW_BEARING,0f);
            currentLocation = new LatLng(lat,lng);
            updateCurrentLocationMarker(lat,lng,bearing);
        }
    };*/

    //private void updateCurrentLocationMarker(double lat,double lng,float bearing) {
    private void updateCurrentLocationMarker(Location location) {
        if(currentLocationMarker != null) currentLocationMarker.remove();
        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(location2LatLng(location))
                .anchor(0.5f,0.5f).flat(true).rotation(location.getBearing())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
    }

    private void scale(LatLng... latLngs){
        scale(111,latLngs);
    }

    private void scale(int padding, LatLng... latLngs){
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for(LatLng ll : latLngs){
            if(ll != null) {
                b.include(ll);
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(b.build(),padding));
    }

    void goToLocation(LatLng ll, float zoom, boolean animate){
        CameraUpdate c = CameraUpdateFactory.newLatLngZoom(ll,zoom);
        if(animate) mMap.animateCamera(c);
        else mMap.moveCamera(c);
    }

    //convert 'Location' object to 'LatLng' object
    private LatLng location2LatLng(Location location){
        return new LatLng(location.getLatitude(),location.getLongitude());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_map);
        drivingDetailtsTV = (TextView) findViewById(R.id.TV_after_start_driving);
        distanceTV = (TextView) findViewById(R.id.TV_trip_info_distance);
        durationTV = (TextView) findViewById(R.id.TV_trip_info_duration);
        startDrivingFAB = (FloatingActionButton) findViewById(R.id.FAB_start_driving);
        myApp = (MyApp) getApplicationContext();
        myApp.setMapActivityRunning(true);
        if(myApp.isDrivingServiceRunning() && myApp.getGoogleApiClient()!=null && myApp.getGoogleApiClient().isConnected()){
            //service is running, GoogleApiClient is ready & connected, permissions & settings are granted >> just get GoogleApiClient & init map
            mGoogleApiClient = myApp.getGoogleApiClient();
            initMap();
        } else {
            //nothing is ready >> connect GoogleApiClient, grant permissions, enable settings, then init map
            checkGoogleApiAvailabilityAndGetGoogleApiClient();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*LocalBroadcastManager.getInstance(this).registerReceiver(updateCurrentLocationBroadcast,
                new IntentFilter(Constants.UPDATE_CURRENT_LOCATION_BROADCAST));*/
    }

    @Override
    protected void onDestroy() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(updateCurrentLocationBroadcast);
        myApp.setUpdateUIonLocationAndGeofenceCallback(null);
        myApp.setMapActivityRunning(false);
        if(mSpeechManager != null){
            mSpeechManager.destroy();
            mSpeechManager = null;
        }
        super.onDestroy();
    }

    public void startDriving(View v){
        mProgressDialog = Utils.showProgressDialog(mProgressDialog,this,"Wait...");
        final ParseObject ride = new ParseObject("Ride");
        ride.put("driver_obj",myApp.getDriver());
        ride.put("isMoving",false);
        ride.put("isAvailable",true);
        ride.put("cost_per_person",3500);
        ride.put("travel_start_time",Utils.getCurrentTime());
        ride.put("encoded_path",Constants.ENCODED_PATH);
        ride.put("type","bus");
        ride.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Utils.hideProgressDialog(mProgressDialog);
                if(e==null){
                    myApp.setRide(ride);
                    leavingScheduleDialog(); //show dialog to schedule when bus will start moving
                    //start driving Service
                    Intent s = new Intent(MainMapActivity.this,DrivingService.class);
                    s.setAction(Constants.ACTION_START);
                    startService(s);
                } else {
                    Utils.showAlertWithNoButtons(MainMapActivity.this,"ERROR!",e.getMessage());
                }
            }
        });
    }

    private void hideView(final View v, boolean animate){
        if(animate){
            Animation a = AnimationUtils.loadAnimation(this,R.anim.hide_scale_rotate);
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            v.startAnimation(a);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    private void showView(final View v, boolean animate){
        v.setVisibility(View.VISIBLE);
        if(animate){
            Animation a = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
            v.startAnimation(a);
        }
    }

    private void hideView2(final View v, boolean animate){
        v.setVisibility(View.GONE);
        if(animate){
            Animation a = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
            v.startAnimation(a);
        }
    }

    private void drawPoly (List<LatLng> list) {
        PolylineOptions options = new PolylineOptions()
                .addAll(list).width(8).color(ContextCompat.getColor(MainMapActivity.this,R.color.colorPrimary));
        mMap.addPolyline(options);
    }

    private void addMarker(LatLng latLng, String title){
        if(mMap != null){
            mMap.addMarker(new MarkerOptions().position(latLng).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    private void addPassengerMarker(Passenger passenger){
        //addMarker(passenger.getLocation(),passenger.getName());
        if(mMap != null){
            Marker m = mMap.addMarker(new MarkerOptions().position(passenger.getLocation()).title(passenger.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.notification_passanger)));
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            passengersMarkers.put(passenger.getId(),m);
        }
    }

    private void appOpenedWhileDrivingServiceIsOn_updateUI(){
        onDrivingServiceStarted(); //hide driving FAB, show TextView, draw trajectory
        //show passengers if any
        for(String key : myApp.getPassengers().keySet()) addPassengerMarker(myApp.getPassengers().get(key));
    }

    private void leavingScheduleDialog(){
        final View v = getLayoutInflater().inflate(R.layout.dialog_start_driving,null);
        Calendar c = Calendar.getInstance();
        int minutesIndex = c.get(Calendar.MINUTE);
        int hoursIndex = c.get(Calendar.HOUR);
        int am_pmIndex = c.get(Calendar.AM_PM);

        now = (AppCompatRadioButton) v.findViewById(R.id.dialog_start_driving_radio_now);
        after = (AppCompatRadioButton) v.findViewById(R.id.dialog_start_driving_radio_after);
        after.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked) showView(v.findViewById(R.id.dialog_start_driving_LL_after),true);
                else hideView2(v.findViewById(R.id.dialog_start_driving_LL_after),true);
            }
        });
        at = (AppCompatRadioButton) v.findViewById(R.id.dialog_start_driving_radio_at);
        at.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked) showView(v.findViewById(R.id.dialog_start_driving_LL_at),true);
                else hideView2(v.findViewById(R.id.dialog_start_driving_LL_at),true);
            }
        });

        startDrivingDialogTitleTV = (TextView) v.findViewById(R.id.dialog_start_driving_title_tv);
        at_hour = (WheelPicker) v.findViewById(R.id.dialog_start_driving_wheel_at_hours);
        at_hour.setSelectedItemPosition(hoursIndex);
        at_minute = (WheelPicker) v.findViewById(R.id.dialog_start_driving_wheel_at_minutes);
        at_minute.setSelectedItemPosition(minutesIndex);
        at_am_pm = (WheelPicker) v.findViewById(R.id.dialog_start_driving_wheel_at_am_pm);
        at_am_pm.setSelectedItemPosition(am_pmIndex);
        after_hours = (WheelPicker) v.findViewById(R.id.dialog_start_driving_wheel_after_hours);
        after_hours.setSelectedItemPosition(3);
        after_minutes = (WheelPicker) v.findViewById(R.id.dialog_start_driving_wheel_after_minutes);
        after_minutes.setSelectedItemPosition(3);

        AlertDialog.Builder b = new AlertDialog.Builder(this,R.style.dialog_picker_full_screen)
                .setCancelable(false)
                .setView(v);
        startDrivingTimeDialog = b.create();

        final String[] hours = getResources().getStringArray(R.array.hours);
        final String[] minutes = getResources().getStringArray(R.array.minutes);
        final String[] am_pm = getResources().getStringArray(R.array.am_pm);
        //final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy KK:mm a");
        final SimpleDateFormat df = new SimpleDateFormat("KK:mm a");
        letsGO = (Button) v.findViewById(R.id.dialog_start_driving_button_letsGO);
        letsGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!now.isChecked() && !after.isChecked() && !at.isChecked()) {
                    Toast.makeText(MainMapActivity.this, "Please select 'NOW', 'AFTER', or 'AT' then set time if required", Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    String s = "";
                    if(now.isChecked()){
                        //s = Utils.getCurrentDateAndTime();
                        s = df.format(new Date(System.currentTimeMillis()));
                    } else if(after.isChecked()){
                        int h = Integer.parseInt(hours[after_hours.getCurrentItemPosition()]);
                        int m = Integer.parseInt(minutes[after_minutes.getCurrentItemPosition()]);
                        long after = System.currentTimeMillis() + (h*60*60*1000) + (m*60*1000);
                        s = df.format(new Date(after));
                    } else if(at.isChecked()) {
                        Calendar c = Calendar.getInstance();
                        String ss = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.YEAR) + " ";
                        ss += hours[at_hour.getCurrentItemPosition()]+":";
                        ss += minutes[at_minute.getCurrentItemPosition()]+" ";
                        ss += am_pm[at_am_pm.getCurrentItemPosition()];
                        if(System.currentTimeMillis() > df.parse(ss).getTime()){
                            Toast.makeText(MainMapActivity.this, "This time is already passed!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        s = df.format(df.parse(ss));
                    }
                    startDrivingTimeDialog.dismiss();
                    Log.e(Constants.TAG,"leaving_time: " + s);
                    myApp.getRide().put("leaving_time",s);
                    myApp.getRide().saveInBackground();
                    //Toast.makeText(MainMapActivity.this, "Leaving time : " + s, Toast.LENGTH_LONG).show();
                } catch (java.text.ParseException e) {
                    Toast.makeText(MainMapActivity.this, "ERROR! " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        startDrivingTimeDialog.show();
    }

    private void moveCameraToLocation(LatLng ll){
        CameraUpdate c = CameraUpdateFactory.newLatLng(ll);
        mMap.animateCamera(c);
    }

    /**************************** MENU *****************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_night_mode:
                if (item.isChecked()) {
                    item.setChecked(false);
                    if(mMap != null) mMap.setMapStyle(null); // set default theme
                } else {
                    item.setChecked(true);
                    if(mMap != null) mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.night_mode_map)); //set night mode style
                }
                return true;
            case R.id.menu_item_voice_commands:
                if(item.isChecked()){
                    item.setChecked(false);
                    if(mSpeechManager != null){
                        mSpeechManager.destroy();
                        mSpeechManager = null;
                    }
                } else {
                    item.setChecked(true);
                    if(mSpeechManager==null){
                        setSpeechListener();
                    }
                    else if(!mSpeechManager.ismIsListening()){
                        mSpeechManager.destroy();
                        setSpeechListener();
                    }
                }
                return true;
            case R.id.menu_item_track_me:
                if (item.isChecked()) {
                    item.setChecked(false);
                    trackMe = false;
                } else {
                    item.setChecked(true);
                    trackMe = true;
                }
                return true;
            case R.id.menu_item_where_am_i:
                if(currentLocationMarker != null) moveCameraToLocation(currentLocationMarker.getPosition());
                return true;
            default:
                return false;
        }
    }

    /******************************** Handle Voice Commands *********************************/

    private SpeechRecognizerManager mSpeechManager;

    private void setSpeechListener()
    {
        //check if recoding audio permission is granted. If not, request...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_RECORD_AUDIO);
            return;
        }
        mSpeechManager = new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
            @Override
            public void onResults(ArrayList<String> results) {
                if(results!=null && results.size()>0)
                {
                    StringBuilder sb = new StringBuilder();
                    for (String result : results) {
                        sb.append(result).append(" ");
                    }
                    String voiceCommand = sb.toString();
                    Log.e(Constants.TAG,"voice Command : " + voiceCommand);
                    if(voiceCommand.contains("start") && voiceCommand.contains("driv")){
                        if(!myApp.isDrivingServiceRunning()) startDriving(null); //leavingScheduleDialog();
                        else Toast.makeText(MainMapActivity.this, "Driving is already started", Toast.LENGTH_SHORT).show();
                    } else if((voiceCommand.contains("stop") || voiceCommand.contains("cancel")) && voiceCommand.contains("driv")){
                        if(myApp.isDrivingServiceRunning()){
                            Intent i2 = new Intent(MainMapActivity.this, DrivingService.class);
                            i2.setAction(Constants.ACTION_STOP);
                            startService(i2);
                        }
                    } else if(voiceCommand.contains("now") && startDrivingTimeDialog.isShowing()){
                        if(now != null) now.setChecked(true);
                    } else if(voiceCommand.contains("after") && startDrivingTimeDialog.isShowing()){
                        if(after != null) after.setChecked(true);
                        if(voiceCommand.contains("minute")){
                            int mins = getNumberBefore(voiceCommand,"minute");
                            if(mins >= 0 && mins < 60 && after_minutes != null) after_minutes.setSelectedItemPosition(mins);
                        } else after_minutes.setSelectedItemPosition(0);
                        if(voiceCommand.contains("hour")){
                            int hours = getNumberBefore(voiceCommand,"hour");
                            if(hours >= 0 && hours < 12 && after_hours != null) after_hours.setSelectedItemPosition(hours);
                        } else after_hours.setSelectedItemPosition(0);
                    }  else if(voiceCommand.contains("at") && startDrivingTimeDialog.isShowing()){
                        if(at != null) at.setChecked(true);
                    } else if(voiceCommand.contains("go") && startDrivingTimeDialog.isShowing()){
                        if(letsGO != null) letsGO.performClick();
                    } else if(voiceCommand.toLowerCase().contains("where am i")){
                        if(currentLocationMarker != null) moveCameraToLocation(currentLocationMarker.getPosition());
                    } else if(voiceCommand.contains("scale")){
                        if(mMap != null) scale(Constants.LOCATION_BAU_BEIRUT,Constants.LOCATION_BAU_DEBBIEH);
                    }
                }
                else
                    Log.e(Constants.TAG,"no results !!!");
            }
        });
    }

    //get the number before the words 'minute' or 'hour'
    private int getNumberBefore(String s, String before){
        String[] words = s.split(" ");
        for(int i=0; i < words.length-1; i++){
            if(words[i+1].startsWith(before)) {
                int n;
                try{n = Integer.parseInt(words[i]);}
                catch(NumberFormatException ex){n = getNumberFromWord(words[i]);}
                return n;
            }
        }
        return 0;
    }

    //convert number word (ex: one) to its relative integer (ex: 1)
    private int getNumberFromWord(String s){
        if(s.equals("one")) return 1;
        if(s.equals("two")) return 2;
        if(s.equals("three")) return 3;
        if(s.equals("four")) return 4;
        if(s.equals("five")) return 5;
        if(s.equals("six")) return 6;
        if(s.equals("seven")) return 7;
        if(s.equals("eight")) return 8;
        if(s.equals("nine")) return 9;
        if(s.equals("ten")) return 10;
        if(s.equals("eleven")) return 11;
        if(s.equals("twelve")) return 12;
        return 0;
    }

    /****************** UI callbacks : to update activity UI from services *******************/

    @Override
    public void onLocationUpdated(Location location) {
        updateCurrentLocationMarker(location);
        if(trackMe) moveCameraToLocation(currentLocationMarker.getPosition());
    }

    @Override
    public void onDurationAndDistanceUpdated(int duration, int distance) {
        String t = "";
        if(duration/3600 > 9) t += duration/3600 + ":";
        else if(duration/3600 > 0) t += "0" + duration/3600 + ":";
        if(duration/60 > 9) t += duration/60 + ":";
        else t += "0" + duration/60 + ":";
        if(duration%60 > 9) t += duration%60;
        else t += "0" + duration%60;

        String d = distance/1000f + "";
        if(d.contains(".")) d = d.substring(0,d.indexOf(".")+2);

        durationTV.setText(t);
        distanceTV.setText(d + " KM");
    }

    @Override
    public void onDrivingServiceStarted() {
        //hide FAB and show TextView
        hideView(startDrivingFAB,true);
        //drivingDetailtsTV.setVisibility(View.VISIBLE);
        //drivingDetailtsTV.startAnimation(AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left));
        drivingDetailtsTV.setText("Driving");
        findViewById(R.id.trip_info_LL).setVisibility(View.VISIBLE);
        //draw trajectory
        drawPoly(PolyUtil.decode(Constants.ENCODED_PATH));
        if(myApp.getDrivingStatus() != null) drivingDetailtsTV.setText(myApp.getDrivingStatus());
    }

    @Override
    public void onDrivingServiceStopped() {
        //drivingDetailtsTV.setVisibility(View.GONE);
        drivingDetailtsTV.setText("Click the bottom wheel to start driving");
        durationTV.setText("--:--");
        distanceTV.setText("-- KM");
        findViewById(R.id.trip_info_LL).setVisibility(View.GONE);
        startDrivingFAB.setVisibility(View.VISIBLE);
        //if(currentLocationMarker!=null) currentLocationMarker.remove();
        //remove passengers from map if any
        for(String pKey : passengersMarkers.keySet()){
            passengersMarkers.get(pKey).remove();
        }
        passengersMarkers.clear();
        myApp.getPassengers().clear();
    }

    @Override
    public void onEnteringBauBeirut() {
        drivingDetailtsTV.setText(myApp.getDrivingStatus());
        if(startDrivingDialogTitleTV != null)
            startDrivingDialogTitleTV.setText("Waiting in Beirut campus.\nWhen will you start driving to Debbieh?");
    }

    @Override
    public void onExitingBauBeirut() {
        drivingDetailtsTV.setText(myApp.getDrivingStatus());
    }

    @Override
    public void onEnteringBauDebbieh() {
        drivingDetailtsTV.setText(myApp.getDrivingStatus());
        if(startDrivingDialogTitleTV != null)
            startDrivingDialogTitleTV.setText("Waiting in Debbieh campus.\nWhen will you start driving to Beirut?");
    }

    @Override
    public void onExitingBauDebbieh() {
        drivingDetailtsTV.setText(myApp.getDrivingStatus());
    }

    @Override
    public void onNewPassenger(Passenger passenger) {
        addPassengerMarker(passenger);
    }

    @Override
    public void onPassengerCancelled(Passenger passenger) {
        if(mMap != null){
            Log.e(Constants.TAG,"onPassengerCancelled UI");
            passengersMarkers.get(passenger.getId()).remove();
            passengersMarkers.remove(passenger.getId());
        }
    }

    @Override
    public void onEnteringPassengerArea(Passenger passenger) {
        if(currentLocationMarker != null) goToLocation(currentLocationMarker.getPosition(),16,true);
            //scale(222,passenger.getLocation(),currentLocationMarker.getPosition());
    }

    @Override
    public void onExitingPassengerArea(Passenger passenger) {
        if(passengersMarkers.containsKey(passenger.getId())){
            passengersMarkers.get(passenger.getId()).remove();
            passengersMarkers.remove(passenger.getId());
            scale(Constants.LOCATION_BAU_BEIRUT,Constants.LOCATION_BAU_DEBBIEH);
        }
    }
}