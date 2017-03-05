package com.example.buhalo.lazyir.service;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;

import java.util.ArrayList;


/**
 * Created by buhalo on 21.02.17.
 */

public class BackgroundService extends JobService {

    final String LOG_TAG = "BackGroundService";

    BackgroundBinder binder = new BackgroundBinder();

    public final static int startListeningUdp = 1;
    public final static int stopListeningUdp = -1;
    public final static int startSendingPeriodicallyUdp = 2;
    public final static int stopSendingPeriodicallyUdp = -2;
    public final static int eraseAllTcpConnectons = 666;
    public final static int port = 5667;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        startListeningUdp(port);
        startSendingPeriodicallyUdp(port);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        stopListeningUdp();
        stopSendingPeriodicallyUdp();
        eraseAllTcpConnectons();
        System.out.println("on stop job");
        return true;
    }

//    public IBinder onBind(Intent arg0) {
//        Log.d(LOG_TAG, "onBind");
//        return binder;
//    }

    public void startListeningUdp(int port)
    {
        Log.d("Service","Start listening udp");
        UdpBroadcastManager.getInstance().startUdpListener(this,port);
    }


    public void stopListeningUdp()
    {
        Log.d("Service","Stop listening udp");
      UdpBroadcastManager.getInstance().stopUdpListener();
    }


    public void startSendingPeriodicallyUdp(int port) {
        Log.d("Back Service","Start sending broadcasts periodically");
       UdpBroadcastManager.getInstance().startSendingTask(this,port);
    }

    public void stopSendingPeriodicallyUdp()
    {
        Log.d("Back Service","Stop sending broadcasts periodically");
        UdpBroadcastManager.getInstance().stopSending();
    }

    public void eraseAllTcpConnectons()
    {
        for(Device dv : Device.connectedDevices.values())
        {
            TcpConnectionManager.getInstance().StopListening(dv.getId());
        }
    }

    @Override
    public void onDestroy() {
        stopListeningUdp();
        stopSendingPeriodicallyUdp();
        eraseAllTcpConnectons();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!= null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                ArrayList<Integer> commands = extras.getIntegerArrayList("Commands");
                if (commands != null) {
                    for (Integer command : commands) {
                        switch (command) {
                            case startListeningUdp:
                                startListeningUdp(port);
                                break;
                            case stopListeningUdp:
                                stopListeningUdp();
                                break;
                            case startSendingPeriodicallyUdp:
                                startSendingPeriodicallyUdp(port);
                                break;
                            case stopSendingPeriodicallyUdp:
                                stopSendingPeriodicallyUdp();
                                break;
                            case eraseAllTcpConnectons:
                                eraseAllTcpConnectons();
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class BackgroundBinder extends Binder {
      public  BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}
