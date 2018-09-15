package com.example.buhalo.lazyir.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;

public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action != null && Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            BackgroundUtil.addCommand(BackgroundServiceCmds.REGISTER_BROADCASTS,context);
        }
    }
}
