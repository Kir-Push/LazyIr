package com.example.buhalo.lazyir.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.TcpConnectionManager;
import com.example.buhalo.lazyir.UdpBroadcastManager;


/**
 * Created by buhalo on 21.02.17.
 */

public class BackgroundService extends Service {

    final String LOG_TAG = "BackGroundService";

    BackgroundBinder binder = new BackgroundBinder();


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public IBinder onBind(Intent arg0) {
        Log.d(LOG_TAG, "onBind");
        return binder;
    }

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

    public class BackgroundBinder extends Binder {
      public  BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}
