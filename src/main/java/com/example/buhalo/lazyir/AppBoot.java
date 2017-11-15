package com.example.buhalo.lazyir;

import android.app.Application;


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

      //  new TestWifiNotifExecuting().test();
       if(checkWifiOnAndConnected(this))
        BackgroundService.startExternalMethod(this);

    }

    private void testClass() {

    }
}
