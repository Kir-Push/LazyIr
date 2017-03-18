package com.example.buhalo.lazyir.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;

import static com.example.buhalo.lazyir.service.BackgroundService.startExternalMethod;
import static com.example.buhalo.lazyir.service.BackgroundService.stopExternalMethod;

/**
 * Created by buhalo on 14.03.17.
 */



public class JasechBroadcastReceiver extends BroadcastReceiver {

    private static boolean lastCheck;
    private static boolean firstTime = true;

    @Override
    public void onReceive(Context context, Intent intent) {


        String action = intent.getAction();
        boolean currCheck = checkWifiOnAndConnected(context);
        if(currCheck == lastCheck && !firstTime)
        {
            return;
        }

        firstTime = false;
        lastCheck = currCheck;


        Log.d("BroadcastReceiver","Some action " + action);
        if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
        {
            if(currCheck) {
                Log.d("BroadcastReceiver","WIFI CONNECTED");
                startExternalMethod(context);
            }
            else {
                Log.d("BroadcastReceiver","WIFI NOT CONNECTED");
                stopExternalMethod(context);
            }
        }
    }

    public static boolean checkWifiOnAndConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            System.out.println("Wifi enabled");
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if(wifiInfo == null || wifiInfo.getNetworkId() == -1){
                return false; // Not connected to an access point
            }

            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }
}
