package com.example.khumalo.dire;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by KHUMALO on 8/15/2016.
 */
public class DirectionService extends IntentService {

    public DirectionService() {
        super("Dire");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Tag","IntentService Called");

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        localIntent.putExtra(Constants.RESULT_EXTRA,"Some String Returned");
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }


}
