package com.example.khumalo.dire;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.khumalo.dire.Utils.Constants;
import com.example.khumalo.dire.R;
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
        final String ORIGIN_PARAM =  "origin";
        final String  DESTINATION_PARAM = "destination";
        final String KEY_PARAM = "key";
        final String REGION_PARAM = "region";


        Uri builtUri = Uri.parse(Constants.FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(ORIGIN_PARAM, "Kenilworth")
                .appendQueryParameter(DESTINATION_PARAM, "Claremont")
                .appendQueryParameter(REGION_PARAM,Constants.REGION_PARAM)
                .appendQueryParameter(KEY_PARAM, getBaseContext().getString(R.string.ApiKey)).build();

        Log.d("Tag", "Url String is " + builtUri.toString());

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        localIntent.putExtra(Constants.RESULT_EXTRA,builtUri.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }


}
