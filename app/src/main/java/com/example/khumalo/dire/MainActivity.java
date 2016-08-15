package com.example.khumalo.dire;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static TextView Display;
    protected ResultReceiver DirectionsReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DirectionsReceiver = new ResultReceiver();
        Display = (TextView)findViewById(R.id.textView);
        Intent intent = new Intent(this,DirectionService.class);
        startService(intent);

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
            Log.d("Tag", "OnReceive Was Called");
            Display.setText(intent.getStringExtra(Constants.RESULT_EXTRA));
        }
    }
}
