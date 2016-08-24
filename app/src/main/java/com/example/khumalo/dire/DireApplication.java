package com.example.khumalo.dire;

import android.content.Context;

import com.firebase.client.Firebase;

/**
 * Created by KHUMALO on 8/18/2016.
 */
public class DireApplication extends android.app.Application {

    public Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getApplicationContext();
        Firebase.setAndroidContext(this);
    }

}
