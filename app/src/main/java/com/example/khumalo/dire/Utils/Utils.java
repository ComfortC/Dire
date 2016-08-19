package com.example.khumalo.dire.Utils;

import com.example.khumalo.dire.Model.Distance;
import com.example.khumalo.dire.Model.Duration;
import com.example.khumalo.dire.Model.Leg;
import com.example.khumalo.dire.Model.Step;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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


}
