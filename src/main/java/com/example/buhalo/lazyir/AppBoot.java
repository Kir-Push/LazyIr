package com.example.buhalo.lazyir;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import com.example.buhalo.lazyir.modules.battery.Battery;
import com.example.buhalo.lazyir.modules.battery.BatteryBroadcastReveiver;
import com.example.buhalo.lazyir.modules.clipBoard.ClipBoard;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.SheluderService;

import java.util.ArrayList;
import java.util.List;

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
