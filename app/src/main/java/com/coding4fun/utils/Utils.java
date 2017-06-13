package com.coding4fun.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.coding4fun.handrider_unibus.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by coding4fun on 06-Nov-16.
 */

public class Utils {

    public static void showAlertWithNoButtons(Context context, String title, String msg){
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(msg)
                .show();
    }

    public static void alertErrorAndExit(Context context, String msg) {
        AlertDialog.Builder d = new AlertDialog.Builder(context);
        d.setCancelable(false);
        d.setTitle("Oppps!");
        d.setMessage(msg + "!\n"+"App will be terminated!");
        d.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.os.Process.killProcess(android.os.Process.myPid()); //exit app (by killing its process)
            }
        });
        d.show();
    }

    public static void notification(Context context,String title,String msg,boolean autoCancel,boolean onGoing){
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder n = new NotificationCompat.Builder(context);
        n.setContentTitle(title);
        n.setContentText(msg);
        n.setTicker(title);
        n.setAutoCancel(autoCancel);	//to prevent cancel it after clicking it
        n.setOngoing(onGoing);
        n.setDefaults(Notification.DEFAULT_ALL);	//default sound, vibration, light...
        n.setSmallIcon(R.mipmap.ic_launcher);
        nm.notify(131, n.build());
    }

    public static ProgressDialog showProgressDialog(ProgressDialog mProgressDialog, Context context, String title) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(title);
        mProgressDialog.show();
        return mProgressDialog;
    }

    public static void hideProgressDialog(ProgressDialog mProgressDialog) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static void pickImage(Context context, Activity activity, int requestCode){
        Intent i = new Intent();
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(i,"Pick a pic"),requestCode);
        Toast.makeText(context, "Pick a pic", Toast.LENGTH_SHORT).show();
    }

    public static String getAbsolutePathFromUri(Context context, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public static String getCurrentDateAndTime(){
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy KK:mm a");
        return df.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentTime(){
        SimpleDateFormat df = new SimpleDateFormat("KK:mm a");
        return df.format(Calendar.getInstance().getTime());
    }

}