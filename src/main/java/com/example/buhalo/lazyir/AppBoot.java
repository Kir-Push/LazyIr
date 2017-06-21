package com.example.buhalo.lazyir;

import android.Manifest;
import android.app.Application;
import android.support.v4.app.ActivityCompat;

import com.example.buhalo.lazyir.service.BackgroundService;


import static com.example.buhalo.lazyir.service.JasechBroadcastReceiver.checkWifiOnAndConnected;

/**
 * Created by buhalo on 21.02.17.
 */

public class AppBoot extends Application {

    final String LOG_TAG = "AppBoot";


    boolean bound = false;

    @Override
    public void onCreate() {


        super.onCreate();

       if(checkWifiOnAndConnected(this))
        BackgroundService.startExternalMethod(this);

    }

    private void testClass() {

    }
}
