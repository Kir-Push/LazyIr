package com.example.buhalo.lazyir.modules.reminder;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.SettingService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by buhalo on 14.01.18.
 */

public class Reminder extends Module {

    private static String REMINDER_TYPE = "Reminder";
    private static String MISSED_CALLS = "MissedCalls";

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
        int callFrequency = Integer.parseInt(settingService.getValue("callFrequency")); // getting int - frequency of timer from setting's in seconds
        Runnable task = () -> {
            DBHelper db = DBHelper.getInstance(BackgroundService.getAppContext());
            List<MissedCall> missedCalls = db.getMissedCalls(); // fetch missed call's
            if(missedCalls.size() > 0){
                NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(REMINDER_TYPE,MISSED_CALLS);
                np.setObject(NetworkPackage.N_OBJECT,new MissedCalls(missedCalls));
                BackgroundService.sendToAllDevices(np.getMessage());
            }
        };
        BackgroundService.getTimerService().scheduleWithFixedDelay(task,callFrequency,callFrequency, TimeUnit.SECONDS);
        callTask = true;
    }


}
