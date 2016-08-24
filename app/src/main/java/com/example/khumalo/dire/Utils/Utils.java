package com.example.khumalo.dire.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.example.khumalo.dire.Model.Distance;
import com.example.khumalo.dire.Model.Duration;
import com.example.khumalo.dire.Model.Leg;
import com.example.khumalo.dire.Model.Step;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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



    public static Leg buildLeg(String forecast) throws JSONException {
        List<Step> legSteps = getLegSteps(forecast);
        int legDistanceValue = getDistanceFromJsonValue(forecast);
        String legDistanceText =  getDistanceFromJson(forecast);
        String legDurationText = getTimeFromJson(forecast);
        Distance legDistance = new Distance(legDistanceText,legDistanceValue);
        int legDurationValue = getTimeFromJsonValue(forecast);
        Duration legDuration = new Duration(legDurationText,legDurationValue);
        Leg leg  = new Leg(legDistance,legDuration,legSteps);
        return leg;
    }

    //Calculating Steps for the first leg in the geoJson String
    public static List<Step> getLegSteps(String geoJson) throws JSONException {
        JSONArray stepArray = getSteps(geoJson);
        List<Step> steps = new ArrayList<Step>();
        for(int i=0; i<stepArray.length();i++){
            int distanceValue = getStepDistance(stepArray,i);
            String distanceText = getStepDistanceText(stepArray,i);
            Distance distance = new Distance(distanceText,distanceValue);
            int timeValue = getStepTime(stepArray,i);
            String timeText = getStepTimeText(stepArray,i);
            Duration duration = new Duration(timeText,timeValue);
            String stepPolyline = getStepPolyline(stepArray,i);
            Step step = new Step(distance,duration,stepPolyline);
            steps.add(step);
        }
        return steps;
    }



    private static int getDistanceFromJsonValue(String geoJson) throws JSONException {
        JSONObject parser = new JSONObject(geoJson);
        JSONArray contents = parser.getJSONArray("routes");
        JSONObject route = contents.getJSONObject(0);
        JSONArray  legs = route.getJSONArray("legs");
        JSONObject properties = legs.getJSONObject(0);
        return properties.getJSONObject("distance").getInt("value");
    }

    //Time
    private static int getTimeFromJsonValue(String geoJson) throws JSONException {
        JSONObject parser = new JSONObject(geoJson);
        JSONArray contents = parser.getJSONArray("routes");
        JSONObject route = contents.getJSONObject(0);
        JSONArray  legs = route.getJSONArray("legs");
        JSONObject properties = legs.getJSONObject(0);
        return properties.getJSONObject("duration").getInt("value");
    }



    public static String getStepPolyline(JSONArray steps, int position) throws JSONException {
        if(steps.length()<= position){
            return "";
        }
        return steps.getJSONObject(position).getJSONObject("polyline").getString("points");
    }



    public static String getStepDistanceText(JSONArray steps, int position) throws JSONException {
        if(steps.length()<= position){
            return "";
        }
        return steps.getJSONObject(position).getJSONObject("distance").getString("text");
    }


   public static String getStepTimeText(JSONArray steps, int position) throws JSONException {
        if(steps.length()<= position){
            return "";
        }
        return steps.getJSONObject(position).getJSONObject("duration").getString("text");
    }




    public static LatLng getRandomLocation(LatLng point, int radius) {

        List<LatLng> randomPoints = new ArrayList<>();
        List<Float> randomDistances = new ArrayList<>();
        Location myLocation = new Location("");
        myLocation.setLatitude(point.latitude);
        myLocation.setLongitude(point.longitude);

        //This is to generate 10 random points
        for(int i = 0; i<10; i++) {
            double x0 = point.latitude;
            double y0 = point.longitude;

            Random random = new Random();

            // Convert radius from meters to degrees
            double radiusInDegrees = radius / 111000f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(y0);

            double foundLatitude = new_x + x0;
            double foundLongitude = y + y0;
            LatLng randomLatLng = new LatLng(foundLatitude, foundLongitude);
            randomPoints.add(randomLatLng);
            Location l1 = new Location("");
            l1.setLatitude(randomLatLng.latitude);
            l1.setLongitude(randomLatLng.longitude);
            randomDistances.add(l1.distanceTo(myLocation));
        }
        //Get nearest point to the centre
        int indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances));
        return randomPoints.get(indexOfNearestPointToCentre);
    }


    //SharedPreference
    public  static void setDriverKey(String key, Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.DRIVER_KEY,key);
        editor.commit();
    }

    //Get The Driver's Key
    public  static String getDriverKey(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constants.DRIVER_KEY,null);
    }

}
