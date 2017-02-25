package com.example.buhalo.lazyir.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.example.buhalo.lazyir.AppBoot;
import com.example.buhalo.lazyir.UdpBroadcastManager;

/**
 * Created by buhalo on 19.02.17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static int oneAtTime = -1;

    @Override
    public void onReceive(final Context context, Intent intent) {
        System.out.println("ja vhozu v receiver");
        Log.d("NetworkReceiver","Entering receiver + " + UdpBroadcastManager.sending + " " + UdpBroadcastManager.listening);
        if(context == null)
        {
            Log.d("NetworkReceiver","Null");
            return;
        }


        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        android.net.NetworkInfo wifi = connMgr.getActiveNetworkInfo();


        if(wifi == null || wifi.getType() != ConnectivityManager.TYPE_WIFI || !wifi.isConnected() || !wifi.isAvailable())
        {
            Log.d("NetworkStateReceiver","send stopListener message");
            AppBoot.backgroundService.stopListeningUdp();
            AppBoot.backgroundService.stopSendingPeriodicallyUdp();
            AppBoot.backgroundService.eraseAllTcpConnectons();
            oneAtTime = -1;
            return;
        }

        Log.d("NetworkStateReceiver","before check state");
        if(oneAtTime != wifi.getType()) {
            Log.d("NetworkStateReceiver","before check state2");
            if (wifi != null && wifi.getType() == ConnectivityManager.TYPE_WIFI && wifi.isAvailable() && wifi.getType() != oneAtTime) {


                    Log.d("NetworkStateReceiver", "Start on NetworkChange");
                AppBoot.backgroundService.stopListeningUdp();
                AppBoot.backgroundService.stopSendingPeriodicallyUdp();
                AppBoot.backgroundService.eraseAllTcpConnectons();
                AppBoot.backgroundService.startListeningUdp(5667);
                AppBoot.backgroundService.startSendingPeriodicallyUdp(5667);
                    Log.d("NetworkStateReceiver", "Start on NetworkChange End");

            }
            else
            {
                Log.d("NetworkStateReceiver","send stopListener message2");
                AppBoot.backgroundService.stopListeningUdp();
                AppBoot.backgroundService.stopSendingPeriodicallyUdp();
                AppBoot.backgroundService.eraseAllTcpConnectons();
            }
            oneAtTime = wifi.getType();
        }
    }
}