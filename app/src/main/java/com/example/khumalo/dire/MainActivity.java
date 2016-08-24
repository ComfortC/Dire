package com.example.khumalo.dire;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.example.khumalo.dire.DriverModel.DriverLocation;
import com.example.khumalo.dire.DriverModel.DriverProfile;
import com.example.khumalo.dire.Login.LoginActivity;
import com.example.khumalo.dire.Model.Leg;
import com.example.khumalo.dire.Model.Step;
import com.example.khumalo.dire.NotificationCenter.BuildNotification;
import com.example.khumalo.dire.Utils.Constants;
import com.example.khumalo.dire.Utils.PermissionUtils;
import com.example.khumalo.dire.Utils.Utils;
import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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

import android.support.design.widget.FloatingActionButton;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.khumalo.dire.Utils.Utils.buildLeg;
import static com.example.khumalo.dire.Utils.Utils.getPolyLineCode;
import static com.example.khumalo.dire.Utils.Utils.getRandomLocation;
import static com.google.maps.android.PolyUtil.decode;

import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    private static final String Tag = "Tag";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PLACE_PICKER_REQUEST = 2;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 3;
    private boolean mPermissionDenied = false;
    Leg leg;
    Polyline mainPolyline;
    GoogleMap mMap;
    ResultReceiver DirectionsReceiver;
    List<LatLng> PolyLocations;
    Marker Driver;
    List<LatLng> stepEnds;
    private ProgressDialog progressDialog;
    String current_Place_extra;
    TextView DistanceToArrival;
    TextView TimeToArrival;
    String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleClient();
        AddDriver();
        AddLocation();
      /*  SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(Constants.isLoggedIn, false)) {
            setContentView(R.layout.activity_main);
            DistanceToArrival = (TextView) findViewById(R.id.tvDistance);
            TimeToArrival = (TextView) findViewById(R.id.tvDuration);
            Toolbar topToolBar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(topToolBar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            DirectionsReceiver = new ResultReceiver();
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.find_me_a_ride);
            topToolBar.bringToFront();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mainPolyline!=null) {
                        mainPolyline.remove();
                    }
                    buildPlacePickerAutoCompleteDialog();
                }
            });
            buildPlacePickerAutoCompleteDialog();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }*/
    }

    private void AddDriver() {
        Firebase database = new Firebase(Constants.FIREBASE_URL).child(Constants.DRIVERS_URL);
        Firebase keyRef = database.push();
        String keyID = keyRef.getKey();
        Utils.setDriverKey(keyID, this);
        DriverProfile driver = new DriverProfile("Comfort","Chinondiwana");
        keyRef.setValue(driver);
    }


    private void AddLocation(){
        String keyID = Utils.getDriverKey(this);
        Firebase database = new Firebase(Constants.FIREBASE_URL).child(Constants.LOCATIONS_URL).child(keyID);
        DriverLocation here = new DriverLocation(-33.9943326,18.4655921);
        database.setValue(here);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(Constants.isLoggedIn, false);
            editor.commit();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
    public class ResultReceiver extends BroadcastReceiver {

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
            try {
                leg = buildLeg(result);


                stepEnds = buildStepEnds();


                // PolylineOptions rectOptions = new PolylineOptions().color(Color.MAGENTA).width(25).addAll(stepPoline);
                Log.d("Tag", "The Smaller Polyline " + String.valueOf(stepEnds.size()));
                // mMap.addPolyline(rectOptions);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("Tag", "The Larger Polyline " + String.valueOf(PolyLocations.size()));
            updateMap(mMap, PolyLocations);

        }
    }

    @NonNull
    private List<LatLng> buildStepEnds() {
        List<LatLng> stepEnds = new ArrayList<LatLng>();

        for (Step step : leg.getSteps()) {
            List<LatLng> span = decode(step.getStepPolyline());
            int lastPosition = span.size() - 1;
            stepEnds.add(span.get(lastPosition));
        }
        return stepEnds;
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
        int finalPosition = polyLocations.size() - 1;
        drawPolylineOriginToDestination(mMap, polyLocations);
        addOrigitToDestinationMarkers(mMap, polyLocations, finalPosition);
        moveCameraToPosition(mMap, polyLocations, finalPosition);
    }


    //Drawing a polyline on the Map
    private void drawPolylineOriginToDestination(GoogleMap mMap, List<LatLng> polyLocations) {
        PolylineOptions rectOptions = new PolylineOptions()
                .addAll(polyLocations);
        mainPolyline = mMap.addPolyline(rectOptions);
    }

    //Addition of Markers Between origin and destination
    private void addOrigitToDestinationMarkers(GoogleMap mMap, List<LatLng> polyLocations, int finalPosition) {
        MarkerOptions Origin = new MarkerOptions().position(polyLocations.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        MarkerOptions destination = new MarkerOptions().position(PolyLocations.get(finalPosition))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        Driver = mMap.addMarker(Origin);
        mMap.addMarker(destination);
    }

    //Moving Camera to cover the two positions
    private void moveCameraToPosition(GoogleMap mMap, final List<LatLng> polyLocations, int finalPosition) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(polyLocations.get(0)).include(polyLocations.get(finalPosition));
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 500), 4000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                AdewaleAnimator animator = new AdewaleAnimator(polyLocations, Driver, stepEnds);
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
                .addApi(LocationServices.API)
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

        }else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            current_Place_extra = mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
            Log.d("Tag", "Place co-ordinates are " +current_Place_extra );
        }


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

        } else {
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
    private void buildPlacePickerAutoCompleteDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

        } else {
            Log.d(Tag, "The Location Access has been Granted");
            try {
                Toast toast = Toast.makeText(getBaseContext(), "Where are you going today Comfort?", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                LatLngBounds CapeTown = new LatLngBounds(new LatLng(-34.307222, 18.416507), new LatLng(-30.892878, 24.217288));
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .setBoundsBias(CapeTown)
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
                LatLng random= new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                LatLng result = getRandomLocation(random, 7000);
                String randomResult = result.latitude+","+result.longitude;
                destination = place.getId();
                progressDialog = ProgressDialog.show(this, "Please wait.",
                        "Searching for your ride...!", true);
                Intent intent = new Intent(this, DirectionService.class);
                intent.putExtra(Constants.CAR_LOCATION,randomResult);
                intent.putExtra(Constants.DESTINATION_EXTRA, place.getId());
                intent.putExtra(Constants.CURRENT_LOCATION,current_Place_extra);
                startService(intent);
                new DownloadRawData().execute();

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


    ///Animation Class
    public class AdewaleAnimator {

        private Marker trackingMarker;
        List<LatLng> steps;

        int stepNumber=0;

        public AdewaleAnimator(List<LatLng> markers, Marker trackingMarker, List<LatLng> steps) {
            this.Directions = markers;
            this.trackingMarker = trackingMarker;
            this.steps = steps;
        }

        public void beginAnimation() {
            mHandler.post(animator);
        }

        private final Handler mHandler = new Handler();
        private Animator animator = new Animator();
        List<LatLng> Directions;

        public class Animator implements Runnable {

            private static final int ANIMATE_SPEEED = 1000;

            private final LinearInterpolator interpolator = new LinearInterpolator();

            int currentIndex = 0;

            long start = SystemClock.uptimeMillis();


            public void stop() {
                trackingMarker.remove();
                mHandler.removeCallbacks(animator);
                BuildNotification.generateNotification(getBaseContext());
            }


            public void stopAnimation() {
                 animator.stop();
            }


            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                double t = interpolator.getInterpolation((float) elapsed / ANIMATE_SPEEED);

                LatLng endLatLng = getEndLatLng();
                LatLng beginLatLng = getBeginLatLng();

                double lat = t * endLatLng.latitude + (1 - t) * beginLatLng.latitude;
                double lng = t * endLatLng.longitude + (1 - t) * beginLatLng.longitude;
                LatLng newPosition = new LatLng(lat, lng);

                if(isMatch(newPosition)){
                    //Log.d("Tag", "ExcutedEqual "+newPosition.toString());
                    DistanceToArrival.setText(leg.getRemainingDistance(stepNumber));
                    TimeToArrival.setText(leg.getRemainingTime(stepNumber));
                    stepNumber++;
                }
                trackingMarker.setPosition(newPosition);

                if (t < 1) {
                    mHandler.postDelayed(this, 16);
                } else {
                    // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                    if (currentIndex < Directions.size() - 2) {

                        currentIndex++;

                        start = SystemClock.uptimeMillis();
                        mHandler.postDelayed(animator, 16);

                    } else {
                        currentIndex++;
                        stopAnimation();
                    }

                }
            }

            private boolean isMatch(LatLng endLatLng) {
                if( stepNumber>=steps.size()){
                    return false;
                }
                Location onPolyline = convertLatLngToLocation(endLatLng);
                Location onStep = convertLatLngToLocation(steps.get(stepNumber));
                float distanceInMeters = onPolyline.distanceTo(onStep);
                return distanceInMeters<100;
            }

            private LatLng getEndLatLng() {
                return Directions.get(currentIndex + 1);
            }

            private LatLng getBeginLatLng() {
                return Directions.get(currentIndex);
            }


        }

        private Location convertLatLngToLocation(LatLng latLng) {
            Location loc = new Location("someLoc");
            loc.setLatitude(latLng.latitude);
            loc.setLongitude(latLng.longitude);
            return loc;
        }

    }




    ///Async Task for the to destination

    public class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            Log.d("Tag","AsyncTask Called");
            final String ORIGIN_PARAM =  "origin";
            final String  DESTINATION_PARAM = "destination";
            final String KEY_PARAM = "key";
            final String REGION_PARAM = "region";

            final String PLACE_ID_PREFIX = "place_id:"+ destination;

            Uri builtUri = Uri.parse(Constants.FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(ORIGIN_PARAM, current_Place_extra)
                    .appendQueryParameter(DESTINATION_PARAM, PLACE_ID_PREFIX)
                    .appendQueryParameter(REGION_PARAM,Constants.REGION_PARAM)
                    .appendQueryParameter(KEY_PARAM, getBaseContext().getString(R.string.ApiKey)).build();
            String Directions =  DownloadDirections(builtUri.toString());

            return Directions;
        }


        @Override
        protected void onPostExecute(String res) {
            String thisLocationToDestination="";
            List<LatLng> polylineToDestination;
            try {
                thisLocationToDestination= getPolyLineCode(res);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            polylineToDestination = decode(thisLocationToDestination);
            drawPolylineCurrentPlaceToDestanation(mMap,polylineToDestination);
            addMarkerToDestination(mMap,polylineToDestination,polylineToDestination.size()-1);

        }




        private String DownloadDirections(String Uri){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {

                URL url = new URL(Uri);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return "";
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return "";
                }

                return buffer.toString();


            } catch (IOException e) {
                Log.e("Tag", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return " ";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Tag", "Error closing stream", e);
                    }
                }
            }
        }


        private void addMarkerToDestination(GoogleMap mMap, List<LatLng> polyLocations, int finalPosition) {

            MarkerOptions destination = new MarkerOptions().position(polyLocations.get(finalPosition))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

            mMap.addMarker(destination);
        }

        private void drawPolylineCurrentPlaceToDestanation(GoogleMap mMap, List<LatLng> polyLocations) {
            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            polylineOptions.addAll(polyLocations);
            mainPolyline = mMap.addPolyline(polylineOptions);
        }

    }
}
