package com.example.lioncode.sec;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class Geof extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final String GEOFENCE_ID = "MyGeofenceId";

    GoogleApiClient googleApiClient = null;

    private Button startLocationMonitoring;
    private Button startGeofenceMonitoring;
    private Button stopGeofenceMonitoring;

    SecDbHandler dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geof);
        dh = new SecDbHandler(this, null, null, 1);

        final Intent in = new Intent();
        final String email = getIntent().getStringExtra("EML");
        final String password = getIntent().getStringExtra("PWD");


        startLocationMonitoring = (Button) findViewById(R.id.startLoc);
        startLocationMonitoring.setOnClickListener(new View.OnClickListener() {
            // Code here executes on main thread after user presses button
            public void onClick(View v) {
                startLocationMonitoring();
                Toast.makeText(getApplicationContext(), "location on",Toast.LENGTH_SHORT).show();
            }

        });

        startGeofenceMonitoring = (Button) findViewById(R.id.startG);
        startGeofenceMonitoring.setOnClickListener(new View.OnClickListener() {
            // Code here executes on main thread after user presses button
            public void onClick(View v) {
                startGeofenceMonitoring();
                Student istu = dh.findStudent(email);
                String icin = istu.getClockin();
                String icout = istu.getClockout();
                Boolean up = dh.updateTime(email, password, icin, icout);

                if(up){
                   // onHandleIntent(in);
                    Toast.makeText(getApplicationContext(), "Clocked in",Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getApplicationContext(), "Did not work",Toast.LENGTH_SHORT).show();
                }
            }
        });
        stopGeofenceMonitoring = (Button) findViewById(R.id.stopGeo);
        stopGeofenceMonitoring.setOnClickListener(new View.OnClickListener() {
            // Code here executes on main thread after user presses button
            public void onClick(View v) {
                stopGeofenceMonitoring();
                Student ostu = dh.findStudent(email);
                String ocin = ostu.getClockin();
                String ocout = ostu.getClockout();
                Boolean up = dh.updateTime(email, password, ocin, ocout);

                if(up){
                    Toast.makeText(getApplicationContext(), "Clocked out",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Did not work",Toast.LENGTH_SHORT).show();
                }

            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                //accesses location service api
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    //handles connections
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "Connected to GoogleApiClient");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Suspended connection to GoogleApiClient");
                    }
                })
                //handles failed connections
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "Failed to connect to GoogleApiClient - " + result.getErrorMessage());
                    }
                })
                .build();
        //googleApiClient.connect();

        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},1234);

    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();

        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (response != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services not available - show dialog to ask usrer to download it");
            GoogleApiAvailability.getInstance().getErrorDialog(this, response, 1).show();
        } else {
            Log.d(TAG, "Google Play Service is available - no action is required");
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart called");
        super.onStart();
        googleApiClient.reconnect();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called");
        super.onStop();
        googleApiClient.disconnect();
    }

    public void startLocationMonitoring() {
        Log.d(TAG, "startLocation called");
        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10000)
                    .setFastestInterval(5000)
                    //.setNumUpdate
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "Location update lat/long " + location.getLatitude() + " " + location.getLongitude());
                    Toast.makeText(getApplicationContext(), "location set",Toast.LENGTH_SHORT).show();
                }
            });
        }catch (SecurityException e) {
            Log.d(TAG, "SecurityException - " + e.getMessage());

        }

    }

    public void startGeofenceMonitoring() {
        Log.d(TAG, "startMonitoring called");
        try {
           // googleApiClient.connect();


            Geofence geofence = new Geofence.Builder()
                    .setRequestId(GEOFENCE_ID)
                    .setCircularRegion(35.944802, -97.261335, 100)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    //monitor the user entering and exiting the geofence
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            GeofencingRequest geofenceRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence).build();
            Toast.makeText(getApplicationContext(), "Geo starts",Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, GeofenceService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (!googleApiClient.isConnected()) {
                Log.d(TAG, "GoogleApiClient Is not connected");
            } else {
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofenceRequest, pendingIntent)
                        .setResultCallback(new ResultCallback<Status>() {

                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    Log.d(TAG, "Successfully added geofence");
                                } else {
                                    Log.d(TAG, "Failed to add geofence + " + status.getStatus());
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "SecurityException - " + e.getMessage());
        }
    }
    public void stopGeofenceMonitoring() {
        Log.d(TAG, "stopMonitoring called");
        ArrayList<String> geofenceIds = new ArrayList<String>();
        geofenceIds.add(GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceIds);
        Toast.makeText(getApplicationContext(), "Geo stops",Toast.LENGTH_SHORT).show();

    }

}
