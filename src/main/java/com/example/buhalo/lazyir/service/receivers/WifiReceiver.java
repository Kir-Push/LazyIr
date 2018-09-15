package com.example.buhalo.lazyir.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import lombok.Getter;
import lombok.Setter;

import static com.example.buhalo.lazyir.service.BackgroundUtil.checkWifiOnAndConnected;


public class WifiReceiver extends BroadcastReceiver {

    @Setter @Getter
    private static boolean lastCheck;



    @Override
    public void onReceive(Context context, Intent intent) {
            boolean currCheck = checkWifiOnAndConnected(context);
            if (currCheck == isLastCheck()) {
                return;
            }
            setLastCheck(currCheck);
                if (currCheck) {
                    BackgroundUtil.addCommand(BackgroundServiceCmds.START_TASKS,context);
                } else {
                    BackgroundUtil.addCommand(BackgroundServiceCmds.DESTROY,context);
                }
    }











}
