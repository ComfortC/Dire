package com.example.khumalo.dire.MarkerAnimation;

import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by KHUMALO on 8/16/2016.
 */
public class AdewaleAnimator {

    private Marker trackingMarker;

    public AdewaleAnimator(List<LatLng> markers, Marker trackingMarker) {
        this.Directions = markers;
        this.trackingMarker =trackingMarker;
    }

     public void beginAnimation(){
        mHandler.post(animator);
     }
    private final Handler mHandler = new Handler();
    private Animator animator = new Animator();
    List<LatLng> Directions;

    public class Animator implements Runnable {

        private static final int ANIMATE_SPEEED = 1000;



        private final LinearInterpolator interpolator = new LinearInterpolator();

        int currentIndex = 0;



        long start = SystemClock.uptimeMillis();



        public void stop() {
            trackingMarker.remove();
            mHandler.removeCallbacks(animator);

        }









        public void stopAnimation() {
            animator.stop();
        }



        @Override
        public void run() {

            long elapsed = SystemClock.uptimeMillis() - start;
            double t = interpolator.getInterpolation((float) elapsed / ANIMATE_SPEEED);

			LatLng endLatLng = getEndLatLng();
			LatLng beginLatLng = getBeginLatLng();

            double lat = t * endLatLng.latitude + (1 - t) * beginLatLng.latitude;
            double lng = t * endLatLng.longitude + (1 - t) * beginLatLng.longitude;
            LatLng newPosition = new LatLng(lat, lng);

            trackingMarker.setPosition(newPosition);



            // It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
            //navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
            //navigateToPoint(newPosition,false);

            if (t < 1) {
                mHandler.postDelayed(this, 16);
            } else {


                // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                if (currentIndex < Directions.size() - 2) {

                    currentIndex++;

                    start = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);

                } else {
                    currentIndex++;
                    stopAnimation();
                }

            }
        }


        private LatLng getEndLatLng() {
            return Directions.get(currentIndex + 1);
        }

        private LatLng getBeginLatLng() {
            return Directions.get(currentIndex);
        }


    }











    /* Highlight the marker by index.
    */



private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
        }

private float bearingBetweenLatLngs(LatLng begin,LatLng end) {
        Location beginL= convertLatLngToLocation(begin);
        Location endL= convertLatLngToLocation(end);
        return beginL.bearingTo(endL);
        }



}
