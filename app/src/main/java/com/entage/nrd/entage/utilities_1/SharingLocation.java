package com.entage.nrd.entage.utilities_1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SharingLocation extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "SharingLocation";


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15;
    private Location  mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    //private Address address;
    private Marker markerCenter;
    private Geocoder geo;


    private TextView accuracy, location_name_1, location_name_2;
    private RelativeLayout send_my_location, send_marker_location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        init();

        // Initialize Places.
        //Places.initialize(getApplicationContext(), getString(R.string.google_map_api_key));

        // Create a new Places client instance.
        //mPlacesClient = Places.createClient(this);

        // Construct a GeoDataClient.
        //GeoDataClient mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        //PlaceDetectionClient mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationUpdates();

        geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    private void init(){
        TextView mTextBack = findViewById(R.id.titlePage);
        mTextBack.setText(getString(R.string.get_location));
        ImageView back = findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        location_name_1 = findViewById(R.id.location_name_1);
        location_name_2 = findViewById(R.id.location_name_2);
        accuracy = findViewById(R.id.accuracy);

        send_my_location = findViewById(R.id.send_my_location);
        send_my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(mLastKnownLocation!= null && mLastKnownLocation.getLongitude() > 0){
                    Address address = getAddress(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), false);

                    intent.putExtra("lat_lng", mLastKnownLocation.getLatitude()+","+mLastKnownLocation.getLongitude());
                    intent.putExtra("address", address);

                    setResult(RESULT_OK, intent);
                }

                onBackPressed();
            }
        });

        send_marker_location  = findViewById(R.id.send_marker_location);
        send_marker_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(markerCenter!= null && markerCenter.getPosition().latitude > 0){
                    Address address = getAddress(markerCenter.getPosition().latitude, markerCenter.getPosition().longitude, true);

                    LatLng latLng =  markerCenter.getPosition();

                    intent.putExtra("lat_lng", latLng.latitude+","+latLng.longitude);
                    intent.putExtra("address", address);

                    setResult(RESULT_OK, intent);
                }

                onBackPressed();
            }
        });
    }

    public void requestLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // each 5 secondes ...... 120000: two minute interval
        mLocationRequest.setFastestInterval(5000);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        updateLocation(location);

                        if(send_marker_location.getVisibility() == View.VISIBLE && markerCenter.getPosition().latitude != 0.0){
                            getAddress(markerCenter.getPosition().latitude, markerCenter.getPosition().longitude, true);
                        }
                    }
                };
            };

            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mMap.getCameraPosition().target);
        markerCenter = mMap.addMarker(markerOptions);

        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw =(BitmapDrawable) getDrawable(R.drawable.ic_location_1);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

       // BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_1);
        markerCenter.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            public void onCameraMove() {
                if(send_marker_location.getVisibility() == View.GONE){
                    send_marker_location.setVisibility(View.VISIBLE);
                }
                markerCenter.setPosition(mMap.getCameraPosition().target);
            }
        });
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (checkLocationPermission()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (checkLocationPermission()) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                if(locationResult != null){
                    locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                // Set the map's camera position to the current location of the device.
                                Location  location  = (Location) task.getResult();
                                updateLocation(location);
                              } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
                }
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocation(Location  location){
        if(location != null){
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float _accuracy = location.getAccuracy();

            if(mLastKnownLocation == null || _accuracy < mLastKnownLocation.getAccuracy()){

                float zoom = mMap.getCameraPosition().zoom;
                if(mLastKnownLocation == null){
                    zoom = DEFAULT_ZOOM;
                }

                if(send_marker_location.getVisibility() == View.GONE){ // dont move if user try to select location by marker
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
                }

                mLastKnownLocation = location;

                accuracy.setText((int)_accuracy+"");
                getAddress(location.getLatitude(), location.getLongitude(), false);
            }
        }
    }

    private Address getAddress(double lat, double lng, boolean isSelectedLocation){
        try {
            List<Address> addresses = geo.getFromLocation(lat, lng, 1);
            if (addresses!=null && !addresses.isEmpty()) {
                Address address =  addresses.get(0);

                String add =  address.getSubAdminArea()+","+address.getAddressLine(0);
                if(!isSelectedLocation){
                    location_name_1.setText(add);
                    location_name_1.setVisibility(View.VISIBLE);
                }else {
                    location_name_2.setText(add);
                    location_name_2.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "setLocation: " + addresses.get(0).toString());

                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}