package com.hlacab.hlacaptain;

import android.app.Application;

import com.teliver.sdk.core.Teliver;

/**
 * Created by gopinath on 25/12/17.
 */

public class Integrate extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Teliver.init(this,"400a7757cea37d9c1276da89bc863470");
    }
}
