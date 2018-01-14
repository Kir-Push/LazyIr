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


    public static void markCallLogRead(int id) { // todo !!

//        Uri CALLLOG_URI = CallLog.Calls.CONTENT_URI;
//        ContentValues values = new ContentValues();
//        values.put("is_read", true);
//        try{
//           BackgroundService.getAppContext().getContentResolver().update(CALLLOG_URI, values, "_id=?",
//                    new String[] { String.valueOf(id) });
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }
}
