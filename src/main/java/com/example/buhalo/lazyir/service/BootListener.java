package com.example.buhalo.lazyir.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.buhalo.lazyir.modules.battery.BatteryBroadcastReveiver;
import com.example.buhalo.lazyir.modules.notificationModule.call.CallListener;
import com.example.buhalo.lazyir.modules.notificationModule.sms.SmsListener;

/**
 * Created by buhalo on 19.11.17.
 */

public class BootListener extends BroadcastReceiver {

    public static volatile boolean registered;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null)
            return;
        String action = intent.getAction();
        if (action != null && Intent.ACTION_BOOT_COMPLETED.equals(action) && !registered) {
            if(BackgroundService.getAppContext() == null)
                BackgroundService.setAppContext(context.getApplicationContext());
           registerBroadcasts(context);
        }
    }

    public synchronized static void registerBroadcasts(Context context) {
        if(registered)
            return;
        registered = true;
        WifiListener receiver = new WifiListener();
        context.getApplicationContext().registerReceiver(receiver,new IntentFilter("android.net.wifi.supplicant.STATE_CHANGE"));

        BatteryBroadcastReveiver batteryBroadcastReveiver = new BatteryBroadcastReveiver();
        context.getApplicationContext().registerReceiver(batteryBroadcastReveiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        SmsListener smsListener = new SmsListener();
        context.getApplicationContext().registerReceiver(smsListener,new IntentFilter( "android.provider.Telephony.SMS_RECEIVED"));

        CallListener callListener = new CallListener();
        context.getApplicationContext().registerReceiver(callListener,new IntentFilter("android.intent.action.PHONE_STATE"));
    }
}
