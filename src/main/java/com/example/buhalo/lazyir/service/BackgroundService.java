package com.example.buhalo.lazyir.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;

import java.util.ArrayList;


/**
 * Created by buhalo on 21.02.17.
 */

public class BackgroundService extends Service {

    final String LOG_TAG = "BackGroundService";

    BackgroundBinder binder = new BackgroundBinder();

    public final static int startListeningTcp = 1;
    public final static int stopListeningTcp = -1;
    public final static int startSendingPeriodicallyUdp = 2;
    public final static int stopSendingPeriodicallyUdp = -2;
    public final static int eraseAllTcpConnectons = 666;
    public final static int port = 5667;

    @Override
    public void onCreate() {
        super.onCreate();
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


    public void stopListeningTcp()
    {
        Log.d("Service","Stop listening tcp");
      TcpConnectionManager.getInstance().stopListening();
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

    public void startListeningTcp()
    {
        TcpConnectionManager.getInstance().startListening(port,this); //todo attention
    }

    @Override
    public void onDestroy() {
        stopListeningTcp();
        stopSendingPeriodicallyUdp();
        eraseAllTcpConnectons();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { //todo check all code and call service , not directly stop in code, call startservice from other code
        if(intent!= null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                ArrayList<Integer> commands = extras.getIntegerArrayList("Commands");
                if (commands != null) {
                    for (Integer command : commands) {
                        switch (command) {
                            case startListeningTcp:
                                startListeningTcp();
                                break;
                            case stopListeningTcp:
                                stopListeningTcp();
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

    //todo create send tcp command and something other

    public static void startExternalMethod(Context context)
    {
        Intent tempIntent = new Intent(context.getApplicationContext(), BackgroundService.class);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(BackgroundService.startListeningTcp);
        list.add(BackgroundService.startSendingPeriodicallyUdp);
        tempIntent.putIntegerArrayListExtra("Commands", list);
        context.startService(tempIntent);
    }

    public static void startExternalCustomMethod(Context context,int... backgroundCommand)
    {
        Intent tempIntent = new Intent(context.getApplicationContext(), BackgroundService.class);
        ArrayList<Integer> list = new ArrayList<>();
        for(int i = 0;i<backgroundCommand.length;i++)
        {
            list.add(backgroundCommand[i]);
        }
        tempIntent.putIntegerArrayListExtra("Commands", list);
        context.startService(tempIntent);
    }

    public static void stopExternalMethod(Context context)
    {
        Intent tempIntent = new Intent(context.getApplicationContext(),BackgroundService.class);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(BackgroundService.stopSendingPeriodicallyUdp);
        list.add(BackgroundService.stopListeningTcp);
        list.add(BackgroundService.eraseAllTcpConnectons);
        tempIntent.putIntegerArrayListExtra("Commands",list);
        context.startService(tempIntent);
    }
}
