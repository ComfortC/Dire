package com.example.khumalo.dire;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.khumalo.dire.Utils.Constants;
import com.example.khumalo.dire.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
                .appendQueryParameter(ORIGIN_PARAM, "Claremont")
                .appendQueryParameter(DESTINATION_PARAM, "Green Point")
                .appendQueryParameter(REGION_PARAM,Constants.REGION_PARAM)
                .appendQueryParameter(KEY_PARAM, getBaseContext().getString(R.string.ApiKey)).build();

        Log.d("Tag", "Url String is " + builtUri.toString());

        String Directions =  DownloadDirections(builtUri.toString());
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        localIntent.putExtra(Constants.RESULT_EXTRA,Directions);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

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

}
