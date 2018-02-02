package com.example.buhalo.lazyir.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;

import static com.example.buhalo.lazyir.modules.notificationModule.CallSmsUtils.getName;
import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationListener.SHOW_NOTIFICATION;

/**
 * Created by buhalo on 14.03.17.
 */
public class WifiListener extends BroadcastReceiver {

    private static boolean lastCheck;
    private static boolean firstTime = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            if (BackgroundService.getAppContext() == null) // todo better sync
                BackgroundService.setAppContext(context.getApplicationContext());
            String action = intent.getAction();
            if (action == null)
                return;

            boolean currCheck = checkWifiOnAndConnected(context);
            if (currCheck == lastCheck && !firstTime) {
                return;
            }
            firstTime = false;
            lastCheck = currCheck;

            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                if (currCheck) {
                  //  BackgroundService.addCommandToQueue(BackgroundServiceCmds.destroy);
                    BackgroundService.addCommandToQueue(BackgroundServiceCmds.destroy);
                    BackgroundService.addCommandToQueue(BackgroundServiceCmds.startTasks);
                } else {
                    BackgroundService.addCommandToQueue(BackgroundServiceCmds.destroy);
                }
            }
        }catch (Throwable e){
           Log.e("WifiListener"," On receive error ",e);
        }
    }




    public static boolean checkWifiOnAndConnected(Context context) {
        try {
            if (context == null)
                return false;
            WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiMgr != null && wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                return !(wifiInfo == null || wifiInfo.getNetworkId() == -1);
            } else {
                return false; // Wi-Fi adapter is OFF
            }
        }catch (Throwable e){
            Log.e("WifiListener","checkWifiOnAndConnected error ",e);
            return false;
        }
    }








}
