package com.greendog.androidstudio.getmebacktomyfirstlocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    private TextView title;
    private Location currentLocation;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up SharedPreferences
        mPrefs = getPreferences(Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();

        title = (TextView) findViewById(R.id.title);

        //Check if google apk is installed
        switch(GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())){
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
                Toast.makeText(getApplicationContext(), "Unable to load Google Play Services",Toast.LENGTH_LONG).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(getApplicationContext(), "Please update Google Play Services",Toast.LENGTH_LONG).show();
                break;
        }

        FragmentManager fmanager = getSupportFragmentManager();
        Fragment fragment = fmanager.findFragmentById(R.id.map);
        SupportMapFragment supportmapfragment = (SupportMapFragment)fragment;

        mMap = supportmapfragment.getMap();

        if(mMap!=null)
            mMap.setMyLocationEnabled(true);


        if(mMap == null){
            Log.d("getmeback", "mMap is null");
            Toast.makeText(this.getApplicationContext(), "Problem connecting with the map", Toast.LENGTH_LONG).show();
        }

        String latitude = mPrefs.getString("latitude", null);
        String longitude = mPrefs.getString("longitude", null);
        Double lat, lon;

        if(latitude != null && longitude != null){
            lat = Double.valueOf(latitude);
            lon = Double.valueOf(longitude);

            updateMap(new LatLng(lat, lon));
        }
        else{
            trackLocation(null);
        }
    }

    public void trackLocation(View view){
        mLocationRequest = LocationRequest.create();
        //set high accurancy for the tracking
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(60000);

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this.getApplicationContext(), "connected", Toast.LENGTH_SHORT).show();

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0,
                new android.location.LocationListener() {
                    @Override
                    public void onStatusChanged(String provider, int status,
                                                Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }

                    @Override
                    public void onLocationChanged(final Location location) {
                    }
                });

        currentLocation = mLocationClient.getLastLocation();

        LatLng cp = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        //update the SharedPreferences
        mEditor.putString("latitude", String.valueOf(currentLocation.getLatitude()));
        mEditor.putString("longitude", String.valueOf(currentLocation.getLongitude()));
        mEditor.commit();

        updateMap(cp);
    }

    public void updateMap(LatLng cp){
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(cp)
                .title("Start Position");

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cp, 17f));
        mMap.addMarker(markerOptions);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);



    }
    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this.getApplicationContext(), "Failed to connect to gps", Toast.LENGTH_LONG).show();
    }


}
