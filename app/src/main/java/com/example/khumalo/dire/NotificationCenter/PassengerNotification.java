package com.example.khumalo.dire.NotificationCenter;

import android.app.NotificationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.khumalo.dire.R;

public class PassengerNotification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_notification);
        Toolbar topToolBar = (Toolbar) findViewById(R.id.second_Toolbar);
        setSupportActionBar(topToolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        NotificationManager nMgr = (NotificationManager) getSystemService(getBaseContext().NOTIFICATION_SERVICE);
        nMgr.cancel(001);
    }
}
