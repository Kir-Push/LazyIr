package com.example.buhalo.lazyir.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.modules.battery.BatteryBroadcastReveiver;
import com.example.buhalo.lazyir.modules.clipBoard.ClipBoard;
import com.example.buhalo.lazyir.utils.ExtScheduledThreadPoolExecutor;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by buhalo on 21.02.17.
 */

public class BackgroundService extends Service {

    // executor service for tasks
    public static final ExecutorService executorService = Executors.newCachedThreadPool();
    // executor exclusively for timer's and repeating tasks
    public static final ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);

    final String LOG_TAG = "BackGroundService";

    BackgroundBinder binder = new BackgroundBinder();

    public final static int startListeningTcp = 1;
    public final static int stopListeningTcp = -1;
    public final static int startSendingPeriodicallyUdp = 2;
    public final static int stopSendingPeriodicallyUdp = -2;
    public final static int startListeningUdp = 3;
    public final static int stopListeningUdp = -3;
    public final static int eraseAllTcpConnectons = 666;

    public final static int startListeningClipBoard = 4;
    public final static int removeClipBoardListener = -4;
    public final static int registerBatteryReceiver = 5;
    public final static int unRegisterBatteryRecever = -5;

    public static int port = 5667;

    private BatteryBroadcastReveiver mReceiver;

    private static boolean batteryRegistered = false;

    private static boolean alreadystarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize and setting executors
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);
        // executorService.submit(Communicator::getInstance);
        ((ThreadPoolExecutor)executorService).setKeepAliveTime(10,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
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
        UdpBroadcastManager.stopSending();
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
        TcpConnectionManager.getInstance().startListening(port,this);
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
    public int onStartCommand(Intent intent, int flags, int startId) {
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
                            case startListeningUdp:
                                startListeningUdp(port);
                                break;
                            case stopListeningUdp:
                                stopListeningUdp();
                                break;
                            case startListeningClipBoard:
                                startListeningClipBoard();
                                break;
                            case removeClipBoardListener:
                                removeClipBoardListener();
                                break;
                            case registerBatteryReceiver:
                                registerBatteryReceiver();
                                break;
                            case unRegisterBatteryRecever:
                                unRegisterBatteryRecever();
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

    private void unRegisterBatteryRecever() {
        if(batteryRegistered)
        unregisterReceiver(mReceiver);
        mReceiver = null;
        batteryRegistered = false;
    }

    private void registerBatteryReceiver() {
        if(mReceiver == null)
        {
            mReceiver = new BatteryBroadcastReveiver();
        }
        registerReceiver(mReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryRegistered = true;
    }

    private void removeClipBoardListener() {
        try {
            ClipBoard.removeListener(this);
        }catch (Exception e)
        {
            Log.e("Service","Remove clipboard error",e);
        }
    }

    private void startListeningClipBoard() {
        ClipBoard.setListener(this);
    }

    public class BackgroundBinder extends Binder {
      public  BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    public static void startExternalMethod(Context context)
    {
        if(alreadystarted)
        {
          //  stopExternalMethod(context);
            return;
        }
        Intent tempIntent = new Intent(context.getApplicationContext(), BackgroundService.class);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(BackgroundService.startListeningUdp);
        list.add(BackgroundService.startSendingPeriodicallyUdp);
        list.add(BackgroundService.startListeningClipBoard);
        list.add(BackgroundService.registerBatteryReceiver);
        tempIntent.putIntegerArrayListExtra("Commands", list);
        context.startService(tempIntent);
        alreadystarted = true;
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
        list.add(BackgroundService.stopListeningUdp);
        list.add(BackgroundService.removeClipBoardListener);
        list.add(BackgroundService.unRegisterBatteryRecever);
        list.add(BackgroundService.eraseAllTcpConnectons);
        tempIntent.putIntegerArrayListExtra("Commands",list);
        context.startService(tempIntent);
        alreadystarted = false;
    }
}
