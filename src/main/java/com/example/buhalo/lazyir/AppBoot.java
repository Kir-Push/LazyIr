package com.example.buhalo.lazyir;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

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
    public void onCreate() { //todo сделай отделньый service для scheludera и из него вызывай background serivce и методы в других местах чтоб напрмую не вызывали а через startservice или как там !!!!
                            //todo дада

        super.onCreate(); // todo нах убери
        if(checkWifiOnAndConnected(this))
        BackgroundService.startExternalMethod(this);
//        JobScheduler js =
//                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        JobInfo job = new JobInfo.Builder(
//                0,
//                new ComponentName(this, SheluderService.class))
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
//                .build();
//        js.schedule(job);
    }
}
