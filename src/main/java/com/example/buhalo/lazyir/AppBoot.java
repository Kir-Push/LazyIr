package com.example.buhalo.lazyir;

import android.app.Application;
import android.content.Context;


import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BootListener;

import static com.example.buhalo.lazyir.service.BaseBroadcastReceiver.checkWifiOnAndConnected;

/**
 * Created by buhalo on 21.02.17.
 */

public class AppBoot extends Application {

    final String LOG_TAG = "AppBoot";


    boolean bound = false;

    @Override
    public void onCreate() {


        super.onCreate();

        Context applicationContext = getApplicationContext();
        BootListener.registerBroadcasts(applicationContext);

        if(BackgroundService.getAppContext() == null)
            BackgroundService.setAppContext(applicationContext);
       if(checkWifiOnAndConnected(this))
           BackgroundService.addCommandToQueue(BackgroundServiceCmds.startTasks);

    }

    private void testClass() {

    }
}
