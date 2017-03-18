package com.example.buhalo.lazyir.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by buhalo on 09.03.17.
 */

public class SheluderService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Intent intent = new Intent(this.getApplicationContext(),BackgroundService.class);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(BackgroundService.startListeningTcp);
        list.add(BackgroundService.startSendingPeriodicallyUdp);
        intent.putIntegerArrayListExtra("Commands",list);
        startService(intent);
        System.out.println("on start job");
       // jobFinished(params,true);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Intent intent = new Intent(this.getApplicationContext(),BackgroundService.class);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(BackgroundService.stopSendingPeriodicallyUdp);
        list.add(BackgroundService.stopListeningTcp);
        list.add(BackgroundService.eraseAllTcpConnectons);
        intent.putIntegerArrayListExtra("Commands",list);
        startService(intent);
        System.out.println("on stop job");
        return true;
    }
}
