package com.example.khumalo.dire;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.khumalo.dire.Utils.Constants;
import com.example.khumalo.dire.Utils.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.List;

import static com.example.khumalo.dire.Utils.Utils.getPolyLineCode;
import static com.google.maps.android.PolyUtil.decode;


public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback {

    GoogleMap mMap;
    ResultReceiver DirectionsReceiver;
    List<LatLng> PolyLocations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(-34.353825, 18.473618)));
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


        }
    }
}
