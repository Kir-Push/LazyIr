package com.example.buhalo.lazyir.modules.reminder;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.Notification;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils;
import com.example.buhalo.lazyir.modules.notificationModule.sms.Sms;
import com.example.buhalo.lazyir.modules.notificationModule.sms.SmsPack;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.SettingService;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 14.01.18.
 */

public class Reminder extends Module {

    private static String REMINDER_TYPE = "Reminder";
    private static String MISSED_CALLS = "MissedCalls";
    private static String UNREAD_MESSAGES = "UnreadMessages";

    // todo check notification's for messenger and also set remind about that.
    // todo you need way to identify message notification (or call) from other messenger notifs(like some ad's or messenger specific notification)
    // todo create strategies for most popular messenger's,
    // todo for others just remind when it show on android wear!
    private static Lock staticLock = new ReentrantLock();
    private static boolean callTask = false;
    private static boolean smsTask = false;
    private static boolean messengerTask = false;
    private static  ScheduledFuture<?> callTaskFuture;
    private static  ScheduledFuture<?> smsTaskFuture;
    private static  ScheduledFuture<?> messengerFuture;

    @Override
    public void execute(NetworkPackage np) {
        String type = np.getType();

      // todo dismiss call and sms command from pc

    }

    private static void setCallReminerTask(){
        staticLock.lock();
        try {
            if (callTask || callTaskFuture == null)
                return;
            SettingService settingService = BackgroundService.getSettingManager();
            int callFrequency = Integer.parseInt(settingService.getValue("callFrequency")); // getting int - frequency of timer from setting's in seconds
            Runnable task = () -> {
                DBHelper db = DBHelper.getInstance(BackgroundService.getAppContext());
                List<MissedCall> missedCalls = db.getMissedCalls(); // fetch missed call's
                if (missedCalls.size() > 0) {
                    NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(REMINDER_TYPE, MISSED_CALLS);
                    np.setObject(NetworkPackage.N_OBJECT, new MissedCalls(missedCalls));
                    BackgroundService.sendToAllDevices(np.getMessage());
                }
            };
            callTaskFuture = BackgroundService.getTimerService().scheduleWithFixedDelay(task, callFrequency, callFrequency, TimeUnit.SECONDS);
            callTask = true;
        }finally {
            staticLock.unlock();
        }
    }

    private static void stopCallReminderTask(){
        staticLock.lock();
        try {
        if(callTaskFuture != null){
            callTaskFuture.cancel(true);
        }
        callTask = false;
        }finally {
            staticLock.unlock();
        }
    }

    private static void setSmsReminderTask(){
        staticLock.lock();
        try {
            if (smsTask || smsTaskFuture == null)
                return;
            SettingService settingService = BackgroundService.getSettingManager();
            int smsFrequency = Integer.parseInt( settingService.getValue("smsFrequency")); // getting int - frequency of timer from setting's in seconds
            Runnable task = () -> {
                DBHelper db = DBHelper.getInstance(BackgroundService.getAppContext());
                List<Sms> unreadMessages = db.getUnreadMessages(); // fetch unread Messages
                unreadMessages.addAll(db.getUnreadMMs());
                if (unreadMessages.size() > 0) {
                    NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(REMINDER_TYPE, UNREAD_MESSAGES);
                    np.setObject(NetworkPackage.N_OBJECT, new SmsPack(unreadMessages));
                    BackgroundService.sendToAllDevices(np.getMessage());
                }
            };
            smsTaskFuture = BackgroundService.getTimerService().scheduleWithFixedDelay(task, smsFrequency, smsFrequency, TimeUnit.SECONDS);
            smsTask = true;
        }finally {
            staticLock.unlock();
        }
    }

    private static void stopSmsReminderTask(){
        staticLock.lock();
        try {
            if(smsTaskFuture != null){
                smsTaskFuture.cancel(true);
            }
            smsTask = false;
        }finally {
            staticLock.unlock();
        }
    }

    static void setMessengersReminderTask(){
        List<Notification> pendingNotifications = NotificationUtils.getPendingNotifications();
    }


    public static void startReminderTasks(){
        setCallReminerTask();
        setSmsReminderTask();
    }

    @Override
    public void endWork() {
        if(Device.getConnectedDevices().size() == 1){ // when endWork fires, this device still in connectedDevices
            stopCallReminderTask();
            stopSmsReminderTask();
        }
    }
}
