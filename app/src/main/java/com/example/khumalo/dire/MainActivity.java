package com.example.khumalo.dire;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.khumalo.dire.MarkerAnimation.AdewaleAnimator;
import com.example.khumalo.dire.MarkerAnimation.LatLngInterpolator;
import com.example.khumalo.dire.MarkerAnimation.MarkerAnimation;
import com.example.khumalo.dire.Utils.Constants;
import com.example.khumalo.dire.Utils.Utils;
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

import org.json.JSONException;

import java.util.List;

import static com.example.khumalo.dire.Utils.Utils.getPolyLineCode;
import static com.google.maps.android.PolyUtil.decode;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    GoogleMap mMap;
    ResultReceiver DirectionsReceiver;
    List<LatLng> PolyLocations;
    Marker Driver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar topToolBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        DirectionsReceiver = new ResultReceiver();
        Intent intent = new Intent(this, DirectionService.class);
        startService(intent);

}

    @Override
    public void onMapReady(GoogleMap googleMap) {
      mMap = googleMap;

    }

    @Override
    protected void onResume() {
        super.onResume();
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

            String PolylineCode = null;
            try {
                PolylineCode = getPolyLineCode(intent.getStringExtra(Constants.RESULT_EXTRA));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PolyLocations = decode(PolylineCode);
            Log.d("Tag", PolyLocations.toString());
            updateMap(mMap, PolyLocations);
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


}
