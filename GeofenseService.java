package com.example.lioncode.sec;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceService extends IntentService {

    public static final String TAG = "GeofenceService";

    public GeofenceService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent)  {

        Log.i(TAG, "onHandleIntent");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            //String errorMessage = GeofenceErrorMessages.getErrorString(this,
            //      geofencingEvent.getErrorCode());
            Log.e(TAG, "Goefencing Error " + geofencingEvent.getErrorCode());
            //return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        Log.i(TAG, "geofenceTransition = " + geofenceTransition + " Enter : " + Geofence.GEOFENCE_TRANSITION_ENTER + "Exit : " + Geofence.GEOFENCE_TRANSITION_EXIT);
        if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) || (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)){
            showNotification("Entered", "Moore Hall");
            Toast.makeText(getApplicationContext(), "one",Toast.LENGTH_SHORT).show();
        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i(TAG, "Showing Notification...");
            showNotification("Exited", "Exited the Location");
            Toast.makeText(getApplicationContext(), "two",Toast.LENGTH_SHORT).show();
        } else {
            // Log the error.
            showNotification("Error", "Error");
            Toast.makeText(getApplicationContext(), "three",Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error ");
        }
    }

    public void showNotification(String text, String bigText) {

        Toast.makeText(getApplicationContext(), "in show notification",Toast.LENGTH_SHORT).show();
        // 1. Create a NotificationManager
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // 2. Create a PendingIntent for AllGeofencesActivity
        Intent intent = new Intent(this, Geof.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 3. Create and send a notification
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Title")
                .setContentText(text)
                .setContentIntent(pendingNotificationIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(0, notification);
    }

}
