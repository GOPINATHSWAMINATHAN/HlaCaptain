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

                                            //My account API Key
        Teliver.init(this,"f090dc812d7ac369d89d8a7fda7f8331");
    }
}
