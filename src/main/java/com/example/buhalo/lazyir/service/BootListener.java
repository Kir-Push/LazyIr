package com.example.buhalo.lazyir.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.buhalo.lazyir.modules.battery.BatteryBroadcastReveiver;

/**
 * Created by buhalo on 19.11.17.
 */

public class BootListener extends BroadcastReceiver {

    public static boolean registered;

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

    public static void registerBroadcasts(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("android.intent.action.PHONE_STATE");
        BaseBroadcastReceiver receiver = new BaseBroadcastReceiver();
        context.getApplicationContext().registerReceiver(receiver,filter);
        BatteryBroadcastReveiver batteryBroadcastReveiver = new BatteryBroadcastReveiver();
        context.getApplicationContext().registerReceiver(batteryBroadcastReveiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registered = true;
    }
}
