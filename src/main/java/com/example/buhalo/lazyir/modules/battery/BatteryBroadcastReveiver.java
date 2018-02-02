package com.example.buhalo.lazyir.modules.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import static com.example.buhalo.lazyir.modules.battery.Battery.PERCENTAGE;
import static com.example.buhalo.lazyir.modules.battery.Battery.STATUS;
import static com.example.buhalo.lazyir.service.WifiListener.checkWifiOnAndConnected;

/**
 * Created by buhalo on 18.04.17.
 */

public class BatteryBroadcastReveiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (checkWifiOnAndConnected(context)) {
            try {
            if (Device.getConnectedDevices().size() < 1) {
                return;
            }
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(Battery.class.getSimpleName(), STATUS);
                np.setValue(PERCENTAGE, Integer.toString(level));
                np.setValue(STATUS, Boolean.toString(isCharging)); // true charging, false not
                BackgroundService.sendToAllDevices(np.getMessage());
            }catch (Throwable e){
                Log.e("BatteryBroadcast","onReceiveError ",e);
            }
        }
    }
}
