package com.example.buhalo.lazyir.modules.reminder;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.SettingService;

/**
 * Created by buhalo on 14.01.18.
 */

public class Reminder extends Module {

    // todo check notification's for messenger and also set remind about that.
    // todo you need way to identify message notification (or call) from other messenger notifs(like some ad's or messenger specific notification)
    // todo create strategies for most popular messenger's,
    // todo for others just remind when it show on android wear!
    private static boolean callTask = false;

    @Override
    public void execute(NetworkPackage np) {

    }

    public static void setCallReminerTask(){
        if(callTask)
            return;
     SettingService settingService = BackgroundService.getSettingManager();
        int callFrequency = Integer.parseInt(settingService.getValue("callFrequency"));
        Runnable task = () -> {

        };
    }


}
