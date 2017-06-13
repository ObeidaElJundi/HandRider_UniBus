package com.coding4fun.handrider_unibus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.coding4fun.interfaces.PrepareDataCallbacks;
import com.coding4fun.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by coding4fun on 20-Feb-17.
 */

public class Login extends AppCompatActivity implements PrepareDataCallbacks{

    private MyApp myApp;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        myApp = (MyApp) getApplicationContext();
    }

    public void facebookLoginButtonClicked(View view) {
        //mProgressDialog = Utils.showProgressDialog(mProgressDialog,this,"Logging in...");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, Arrays.asList("public_profile","user_friends","email","user_birthday"), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                //Utils.hideProgressDialog(mProgressDialog);
                if(user == null){
                    Toast.makeText(Login.this, "Login cancelled! :(", Toast.LENGTH_SHORT).show();
                } else if(user.isNew()){
                    Toast.makeText(Login.this, "logged in successfully :)", Toast.LENGTH_SHORT).show();
                    getUserData();
                    saveUserInInstallation();
                } else {
                    Toast.makeText(Login.this, "logged in successfully :)", Toast.LENGTH_SHORT).show();
                    myApp.setPrepareDataCallbacks(Login.this);
                    myApp.prepareData();
                }
            }
        });
    }

    //get user info (email, name, profile pic ... etc) via facebook SDK and save them in parse DB
    private void getUserData(){
        mProgressDialog = Utils.showProgressDialog(mProgressDialog,this,"Wait a moment...");
        GraphRequest gr = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.e("HandRider",String.valueOf(object));
                try {
                    if(object.has("email")) ParseUser.getCurrentUser().put("facebookEmail",object.getString("email"));
                    if(object.has("gender")) ParseUser.getCurrentUser().put("sex",object.getString("gender"));
                    if(object.has("name")) ParseUser.getCurrentUser().put("fullname",object.getString("name"));
                    if(object.has("picture")) ParseUser.getCurrentUser().put("userImageLink",object.getJSONObject("picture").getJSONObject("data").getString("url"));
                    if(object.has("birthday")) ParseUser.getCurrentUser().put("dateOfBirthString",object.getString("birthday"));
                    ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Utils.hideProgressDialog(mProgressDialog);
                            //goToNextActivity();
                            saveNewUserData();
                        }
                    });
                } catch (Exception ex) {
                    Utils.hideProgressDialog(mProgressDialog);
                    Utils.alertErrorAndExit(Login.this,ex.getMessage());
                }
            }
        });
        Bundle params = new Bundle();
        params.putString("fields", "id,name,birthday,email,gender,link,picture.type(large)");
        gr.setParameters(params);
        gr.executeAsync();
    }

    private void saveNewUserData(){
        mProgressDialog = Utils.showProgressDialog(mProgressDialog,this,"Saving your data...");
        ParseQuery<ParseObject> vehicleQuery = ParseQuery.getQuery("Vehicle");
        vehicleQuery.whereEqualTo("objectId", "jPEI7ZTq9t");
        vehicleQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e != null){
                    Utils.hideProgressDialog(mProgressDialog);
                    Utils.alertErrorAndExit(Login.this,e.getMessage());
                    return;
                }
                myApp.setVehicle(objects.get(0));
                final ParseObject driver = new ParseObject("Driver");
                driver.put("user_obj",ParseUser.getCurrentUser());
                driver.put("vehicle_obj",objects.get(0));
                driver.put("rating",0);
                driver.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e != null){
                            Utils.hideProgressDialog(mProgressDialog);
                            Utils.alertErrorAndExit(Login.this,e.getMessage());
                            return;
                        }
                        myApp.setDriver(driver);
                        ParseQuery<ParseObject> universityQuery = ParseQuery.getQuery("University_Location");
                        universityQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                Utils.hideProgressDialog(mProgressDialog);
                                if(e == null) {
                                    myApp.setUniversities(objects);
                                    goToNextActivity();
                                } else {
                                    Utils.alertErrorAndExit(Login.this,e.getMessage());
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void saveUserInInstallation(){
        ParseInstallation.getCurrentInstallation().put("user",ParseUser.getCurrentUser());
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    private void goToNextActivity(){
        startActivity(new Intent(Login.this,MainMapActivity.class));
        Login.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void start() {
        mProgressDialog = Utils.showProgressDialog(mProgressDialog,this,"Getting your data...");
    }

    @Override
    public void done() {
        Utils.hideProgressDialog(mProgressDialog);
        goToNextActivity();
    }

    @Override
    public void error(String error) {
        Utils.hideProgressDialog(mProgressDialog);
        Utils.alertErrorAndExit(this,error);
    }
}