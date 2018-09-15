package com.example.buhalo.lazyir.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.example.buhalo.lazyir.modules.battery.BatteryDto;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import org.greenrobot.eventbus.EventBus;

import static com.example.buhalo.lazyir.service.BackgroundUtil.checkWifiOnAndConnected;


public class BatteryBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (checkWifiOnAndConnected(context)) {
                BackgroundUtil.addCommand(BackgroundServiceCmds.CACHE_CONNECT, context);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                BatteryDto dto = new BatteryDto(Integer.toString(level), Boolean.toString(isCharging));
                EventBus.getDefault().post(dto);
        }
    }
}
