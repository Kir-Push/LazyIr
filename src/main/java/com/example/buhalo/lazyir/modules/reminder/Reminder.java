package com.example.buhalo.lazyir.modules.reminder;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.notificationModule.messengers.Messengers;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.Notification;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.Notifications;
import com.example.buhalo.lazyir.modules.notificationModule.sms.Sms;
import com.example.buhalo.lazyir.modules.notificationModule.sms.SmsPack;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.SettingService;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by buhalo on 14.01.18.
 */

public class Reminder extends Module {

    private final static String REMINDER_TYPE = "Reminder";
    private final static String MISSED_CALLS = "MissedCalls";
    private final static String UNREAD_MESSAGES = "UnreadMessages";
    private final static String DISSMIS_ALL_CALLS = "dismissAllCalls";
    private final static String DISSMIS_ALL_MESSAGES = "dismissAllMessages";
    private final static Lock staticLock = new ReentrantLock();
    private static boolean callTask = false;
    private static boolean smsTask = false;
    private static  ScheduledFuture<?> callTaskFuture;
    private static  ScheduledFuture<?> smsTaskFuture;

    @Override
    public void execute(NetworkPackage np) {
        String type = np.getData();
        switch (type){
            case "dismissCall":
                dismissCalls(np);
                break;
            case DISSMIS_ALL_CALLS:
                dismissAllCalls(np);
                break;
            case DISSMIS_ALL_MESSAGES:
                dismissMessage(np);
                break;
            case "recall":
                recall(np);
                break;
            default:
                break;

        }
    }


    private void dismissAllCalls(NetworkPackage np) {
        String value = np.getValue(MISSED_CALLS);
        if(value != null){
            String[] split = value.split(":::");
            for (String s : split) {
                setCallUnMissed(Integer.parseInt(s));
            }
        }
    }


    // call to number from missedCall object.
    // if missedCall contain many object's - call only for first
    // before calling, call dismissCalls method for set call unMissed.
    private void recall(NetworkPackage np) {

    }

    // get MessagesPack from networkPackage, depending of cointained object's - if sms remove from unread log,
    // if messenger message remove notification
    private void dismissMessage(NetworkPackage np) {
        MessagesPack object = np.getObject(NetworkPackage.N_OBJECT, MessagesPack.class);
        if(object == null)
            return;
        Notifications notifications = object.getNotifications();
        SmsPack smsPack = object.getSmsPack();
        List<Sms> smsList;
        List<Notification> notificationList;
        if(smsPack != null &&(smsList = smsPack.getSms()) != null && smsList.size() > 0){
            for (Sms sms : smsList) {
                setMessageUnread(sms);
            }
        }
        if(notifications != null && (notificationList = notifications.getNotifications()) != null && notificationList.size() > 0){
            Context appContext = BackgroundService.getAppContext();
            NotificationManager mNotifyMgr = (NotificationManager) appContext.getSystemService(NOTIFICATION_SERVICE);
            for (Notification notification : notificationList) {
                setMessageUnread(notification,mNotifyMgr);
            }
        }

    }

    // remove call from missed log's, if list contain more than one missedCall, removing all of it.
    private void dismissCalls(NetworkPackage np) {
        MissedCalls object = np.getObject(NetworkPackage.N_OBJECT, MissedCalls.class);
        if(object == null)
            return;
        List<MissedCall> missedCalls = object.getMissedCalls();
        if(missedCalls != null && missedCalls.size() > 0){
            for (MissedCall missedCall : missedCalls) {
                setCallUnMissed(missedCall);
            }
        }
    }

    private void setCallUnMissed(MissedCall missed){
        setCallUnMissed(Integer.parseInt(missed.getId()));
    }

    private void setCallUnMissed(int id){
        Context appContext = BackgroundService.getAppContext();
        DBHelper.getInstance(appContext).markCallLogRead(id);
    }

    private void setMessageUnread(Sms sms){
        Context appContext = BackgroundService.getAppContext();
        DBHelper.getInstance(appContext).setMessageRead(appContext,Long.valueOf(sms.getId()),true,sms.getType().equals("mms"));
    }
    private void setSMSunread(long id){

    }

    private void setMessageUnread(Notification notification,NotificationManager notificationManager){
        Messengers.getPendingNotifsLocal().remove(notification.getPack() + ":" + notification.getTitle());
        notificationManager.cancel(Integer.parseInt(notification.getId())); // todo test, and if didn't work, do though post notification (send notificaiton with id, and in notif listener remove posted notif and notif by contained id)
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
                List<Notification> pendingNotifications = fetchNotification();
                Notifications notifications = new Notifications(pendingNotifications);
                SmsPack smsPack = new SmsPack(unreadMessages);
                if (unreadMessages.size() > 0) {
                    NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(REMINDER_TYPE, UNREAD_MESSAGES);
                    np.setObject(NetworkPackage.N_OBJECT, new MessagesPack(notifications,smsPack));
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


    private static List<Notification> fetchNotification(){ // todo in future add strategies to for most populat messengers
        return NotificationUtils.getPendingNotifications();
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
