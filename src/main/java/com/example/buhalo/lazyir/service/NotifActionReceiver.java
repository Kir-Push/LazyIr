package com.example.buhalo.lazyir.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.util.Log;

import com.example.buhalo.lazyir.modules.ping.Ping;

import java.io.IOException;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.buhalo.lazyir.service.WifiListener.checkWifiOnAndConnected;

/**
 * Created by buhalo on 01.02.18.
 */

public class NotifActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if( checkWifiOnAndConnected(context)) {
            try {
                String action = intent.getAction();
                NotificationManager mNotifyMgr =
                        (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                if ("Yes".equals(action)) {
                    BackgroundService.pairDevice(intent.getStringExtra("id"), intent.getStringExtra("value"));
                    mNotifyMgr.cancel(775533);
                } else if ("No".equals(action)) {
                    BackgroundService.unpairDevice(intent.getStringExtra("id"));
                    mNotifyMgr.cancel(775533);
                } else if ("TURN OFF".equals(action)) {
                    mNotifyMgr.cancel(6661666);
                    Ping.stopAlarm();
                }
            }catch (Throwable e){
                Log.e("NotifActionReceiver","onReceive Error " ,e);
            }
        }
    }
}
