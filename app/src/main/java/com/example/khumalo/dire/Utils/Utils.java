package com.example.khumalo.dire.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by KHUMALO on 8/16/2016.
 */
public class Utils {



    //Polyline
    public static String getPolyLineCode(String geoJson) throws JSONException {
        JSONObject parser = new JSONObject(geoJson);
        JSONArray contents = parser.getJSONArray("routes");
        JSONObject route = contents.getJSONObject(0);
        JSONObject polyline = route.getJSONObject("overview_polyline");

        return polyline.getString("points");
    }

    //Distance
    public static String getDistanceFromJson(String geoJson) throws JSONException {
        JSONObject parser = new JSONObject(geoJson);
        JSONArray contents = parser.getJSONArray("routes");
        JSONObject route = contents.getJSONObject(0);
        JSONArray  legs = route.getJSONArray("legs");
        JSONObject properties = legs.getJSONObject(0);
        return properties.getJSONObject("distance").getString("text");
    }

    //Time
    public static String getTimeFromJson(String geoJson) throws JSONException {
        JSONObject parser = new JSONObject(geoJson);
        JSONArray contents = parser.getJSONArray("routes");
        JSONObject route = contents.getJSONObject(0);
        JSONArray  legs = route.getJSONArray("legs");
        JSONObject properties = legs.getJSONObject(0);
        return properties.getJSONObject("duration").getString("text");
    }



    //Return the Steps required from origin to destination,

    public static JSONArray getSteps(String geoJson) throws JSONException {
        JSONObject parser = new JSONObject(geoJson);
        JSONArray contents = parser.getJSONArray("routes");
        JSONObject route = contents.getJSONObject(0);
        JSONArray  legs = route.getJSONArray("legs");
        JSONObject properties = legs.getJSONObject(0);
        return properties.getJSONArray("steps");
    }

    //Getting the distance for the Step
     public static  int getStepDistance(JSONArray steps, int position) throws JSONException {
        if(steps.length()<= position){
            return -1;
        }
        return steps.getJSONObject(position).getJSONObject("distance").getInt("value");
    }

    //Getting the time for the step
    public static int getStepTime(JSONArray steps, int position) throws JSONException {
        if(steps.length()<= position){
            return -1;
        }
        return steps.getJSONObject(position).getJSONObject("duration").getInt("value");
    }
}
