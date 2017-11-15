package com.example.buhalo.lazyir.modules.battery;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

/**
 * Created by buhalo on 17.04.17.
 */

// you use it only in one place, when introduce received, maybe you don't need it, you have batterybroadcastreceiver which send to all device battery when system push it
@Deprecated
public class Battery { // it not implement module because it one for all, send message when connected device , and after send to all when broadcast received
    public static final String STATUS = "status";
    public static final String PERCENTAGE = "percentage";

    public static void sendBatteryLevel(String id, Context context)
    {
        NetworkPackage np =   NetworkPackage.Cacher.getOrCreatePackage(Battery.class.getSimpleName(),STATUS);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, ifilter);
        if(batteryStatus == null)
            return;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        String percentage = Integer.toString(level);
        np.setValue(PERCENTAGE,percentage);
        np.setValue(STATUS,Boolean.toString(isCharging)); // true charging, false not
        TcpConnectionManager.getInstance().sendCommandToServer(id,np.getMessage());
    }

}
