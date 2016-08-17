package com.example.khumalo.dire;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.example.khumalo.dire.MarkerAnimation.AdewaleAnimator;
import com.example.khumalo.dire.Utils.Constants;
import com.example.khumalo.dire.Utils.PermissionUtils;
import com.example.khumalo.dire.Utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONException;

import java.util.List;

import static com.example.khumalo.dire.Utils.Utils.getPolyLineCode;
import static com.google.maps.android.PolyUtil.decode;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {


    GoogleApiClient mGoogleApiClient;
    private static final String Tag = "Tag";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final  int PLACE_PICKER_REQUEST = 2;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 3;
    private boolean mPermissionDenied = false;



    GoogleMap mMap;
    ResultReceiver DirectionsReceiver;
    List<LatLng> PolyLocations;
    Marker Driver;

    private ProgressDialog progressDialog;

    TextView DistanceToArrival;
    TextView TimeToArrival;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleClient();
        DistanceToArrival =(TextView) findViewById(R.id.tvDistance);
        TimeToArrival =  (TextView) findViewById(R.id.tvDuration);
        Toolbar topToolBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DirectionsReceiver = new ResultReceiver();

}

    @Override
    public void onMapReady(GoogleMap googleMap) {
      mMap = googleMap;
        Toast toast = Toast.makeText(getBaseContext(), "Where are you going today Comfort?", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL,0,0);
        toast.show();
        buildPlacePickerAutoCompleteDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(DirectionsReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(DirectionsReceiver);
    }



    //Reciver for the DirectionService.
    public class ResultReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialog.dismiss();
            String PolylineCode = null;
            String result = intent.getStringExtra(Constants.RESULT_EXTRA);
            try {
                PolylineCode = getPolyLineCode(result);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            UpdateDistanceTimeTextViews(result);


            PolyLocations = decode(PolylineCode);
            Log.d("Tag", PolyLocations.toString());
            updateMap(mMap, PolyLocations);
        }
    }

    private void UpdateDistanceTimeTextViews(String result) {
        try {
            String distance = Utils.getDistanceFromJson(result);
            String time = Utils.getTimeFromJson(result);
            DistanceToArrival.setText(distance);
            TimeToArrival.setText(time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Setting Up the Locations Retrieved from the DirectionsAPI
    private void updateMap(GoogleMap mMap, List<LatLng> polyLocations) {
        int finalPosition = polyLocations.size()-1;
        drawPolylineOriginToDestination(mMap, polyLocations);
        addOrigitToDestinationMarkers(mMap, polyLocations, finalPosition);
        moveCameraToPosition(mMap, polyLocations, finalPosition);
    }


   //Drawing a polyline on the Map
    private void drawPolylineOriginToDestination(GoogleMap mMap, List<LatLng> polyLocations) {
        PolylineOptions rectOptions = new PolylineOptions()
                           .addAll(polyLocations);
        Polyline polyline = mMap.addPolyline(rectOptions);
    }

    //Addition of Markers Between origin and destination
    private void addOrigitToDestinationMarkers(GoogleMap mMap, List<LatLng> polyLocations, int finalPosition) {
        MarkerOptions Origin = new MarkerOptions().position(polyLocations.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        MarkerOptions destination = new MarkerOptions().position(PolyLocations.get(finalPosition))
                               .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        Driver =  mMap.addMarker(Origin);
        mMap.addMarker(destination);
    }

    //Moving Camera to cover the two positions
    private void moveCameraToPosition(GoogleMap mMap, final List<LatLng> polyLocations, int finalPosition) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(polyLocations.get(0)).include(polyLocations.get(finalPosition));
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200), 4000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                AdewaleAnimator animator = new AdewaleAnimator(polyLocations,Driver);
                animator.beginAnimation();
            }

            @Override
            public void onCancel() {

            }
        });


    }




    ////////GoogleClientImplementation

    ////////////
    protected synchronized void buildGoogleClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

    }



    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(Tag, "Connection has failed");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(Tag, "The client has been connected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Tag, "The connection has been suspended");
    }


    /// Building a Place Picker dialog with a map
    private void buildPlacePickerMapDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

        }else{
            Log.d(Tag, "The Location Access has been Granted");
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }

        }

    }

    ///Building Place AutoComplete without a Dialog
    private void buildPlacePickerAutoCompleteDialog(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

        }else{
            Log.d(Tag, "The Location Access has been Granted");
            try {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(this);
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                // TODO: Handle the error.
            } catch (GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
            }

        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                progressDialog = ProgressDialog.show(this, "Please wait.",
                        "Searching for your ride...!", true);
                Intent intent = new Intent(this, DirectionService.class);
                startService(intent);

            }
        }
    }


    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(Tag, "The Location Access has been Granted");
            buildPlacePickerAutoCompleteDialog();

        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

}
