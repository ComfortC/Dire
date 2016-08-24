package com.example.khumalo.dire.DriverModel;

import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;

import com.example.khumalo.dire.DireApplication;
import com.example.khumalo.dire.MainActivity;
import com.example.khumalo.dire.Utils.Constants;
import com.firebase.client.Firebase;

import junit.framework.TestCase;



/**
 * Created by KHUMALO on 8/24/2016.
 */
public class DriverProfileTest extends AndroidTestCase{


    public void setUp() throws Exception {
        super.setUp();


    }


    public void testInsertDriverIntoDatabase() throws Exception {
        Firebase.setAndroidContext(mContext);
        Firebase database = new Firebase(Constants.FIREBASE_URL).child(Constants.DRIVERS_URL);
        Firebase keyRef = database.push();
        String keyID = keyRef.getKey();
        DriverProfile driver = new DriverProfile("Comfort","Chinondiwana");
        keyRef.setValue(driver);
    }

    public void tearDown() throws Exception {
    }
}