package com.example.buhalo.lazyir.modules.notificationModule.notifications;

import android.app.NotificationManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.SparseLongArray;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.notificationModule.messengers.Messengers;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.SettingService;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils.containsInMap;
import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils.messengerDupStrangeCheck;
import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils.messengerMessageCheckAndSend;
import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils.messengersMessage;
import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils.smsMessage;
import static com.example.buhalo.lazyir.service.WifiListener.checkWifiOnAndConnected;

/**
 * Created by buhalo on 21.03.17.
 */

public class NotificationListener extends NotificationListenerService {
    private static NotificationListener notif;
    public static final String SHOW_NOTIFICATION = "ShowNotification";
    public static final String RECEIVE_NOTIFICATION = "receiveNotification";
    public static final String DELETE_NOTOFICATION = "deleteNotification";
    public static final String SMS_TYPE = "com.android.mms";

    //---------------------------------------------
    public static final String SMS_TYPE_2 = "com.android.messaging";

    private static SparseLongArray notifsToFrequent = new SparseLongArray();
    private static HashMap<String,AbstractMap.SimpleEntry<Long,Integer>> spamDefender = new HashMap<>();
    private  Pattern chargeRegex;

    @Override
    public void onCreate() {
        super.onCreate();
        notif = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notif = null;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if(BackgroundService.getAppContext() == null)
                BackgroundService.setAppContext(getApplicationContext());
            if(!checkWifiOnAndConnected(getApplicationContext())){
                return;
            }
            if(notif == null)
            notif = this;

            if(sbn.getId() == 999888777) {
                removeNotification(sbn.getKey());
                for (StatusBarNotification statusBarNotification : getAll()) {
                    if(statusBarNotification.getId() == Integer.parseInt(NotificationUtils.extractTitle(sbn)))
                        removeNotification(statusBarNotification.getKey());
                }
                return;
            }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && sbn.getId() == BackgroundService.NotifId)
            snoozeNotification(sbn.getKey(), 600000);


            // todo in pc in log file log notifs pack and some info (without text ) чтобы знать какие pack если что игнорить!
        //    SettingService.checkForIgnore(sbn); // todo


            if(!smsMessage(sbn)){                        //first check if notification is not smsMessage, if it is - send as sms
                BackgroundService.addCommandToQueue(BackgroundServiceCmds.cacheConnect);
                Notification notification = NotificationUtils.castToMyNotification(sbn);
                if(!BackgroundService.hasActualConnection()){
                    BackgroundService.getExecutorService().submit(()->{
                        try {
                            Thread.sleep(700); // wait, you have chance to establish connection
                            whatToDoIfNotSms(sbn,notification);
                        }catch (InterruptedException e) {
                            Log.e("NotificationListener","onNotificationPosted error ",e);
                        }
                    });
                }else
                whatToDoIfNotSms(sbn,notification);
            }
        } catch (Throwable e) {                          // i don't need crash app if something going wrong
        Log.e("NotificationListener","onNotificationPosted error",e);
        }

    }

    private void whatToDoIfNotSms(StatusBarNotification sbn,Notification notification){
        if(messengerMessageCheckAndSend(sbn,notification.getPack(),notification.getTitle(),notification.getText(),notification)) {
        }
        else
            sendToAll(sbn, RECEIVE_NOTIFICATION);  // if previous two false, this is notif, send to server
    }

    private void removeNotification(String id){
        cancelNotification(id);
    }


    public static StatusBarNotification[] getAll() {
        System.out.println(notif);
        System.out.println( notif.getActiveNotifications());
      return   notif == null ? null : notif.getActiveNotifications();
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        try {
            if(!checkWifiOnAndConnected(getApplicationContext())){
                return;
            }
            Notification notification = NotificationUtils.castToMyNotification(sbn);
            if (notification != null && notification.getPack() != null) {
                Messengers.getPendingNotifsLocal().remove(notification.getPack() + ":" + notification.getTitle());
            }
            sendToAll(sbn, DELETE_NOTOFICATION);
            Log.i("Msg", "Notification was removed");}
            catch (Exception e) {
            Log.e("NotificationListener",e.toString());
        }
    }

    // send notif to all with some flag ( method arg)
    private void sendToAll(StatusBarNotification sbn,String method)
    {
        try {
            if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.KITKAT) {
                return;
            }
            NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION,method);
            Notification notification = NotificationUtils.castToMyNotification(sbn);
            if(!method.equals(DELETE_NOTOFICATION)) {
                boolean duplicate = checkNotificationForDuplicates(notification);
                boolean charging = checkChargingNotification(notification);
                boolean spam = checkSpecialNotifs(notification);
                if (duplicate || charging || spam)
                    return;
            }
            np.setObject(NetworkPackage.N_OBJECT,notification);
            String message = np.getMessage();
            if(message != null && !message.equals(""))
               BackgroundService.sendToAllDevices(np.getMessage());
        }catch (Exception e) {
            Log.e(SHOW_NOTIFICATION,e.toString());
        }
    }

    //check whether duplicate notificatione was posted around 60 second ago
    private boolean checkNotificationForDuplicates(Notification notification) {
        long currTime = System.currentTimeMillis();
        int hash = notification.hashCode();
        Long aLong = notifsToFrequent.get(hash);
        if (notifsToFrequent.size() >= 20)
            notifsToFrequent.clear();  // don't store too many object's
        notifsToFrequent.put(hash, currTime);
        if (aLong == 0) { // if zero - no data, that means after clearing this notification first - return false
            return false;
        } else {
            long difference = currTime - aLong;
            return difference < 60000;
        }
    }

    // charging notification show each time when percent update, we don't want to see it
    // so filter it
    private boolean checkChargingNotification(Notification notification) {
        if(notification.getPack().equals("com.android.systemui")){ // charging notifs have this packageName
            NotificationManager notificationManager = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
            if(notificationManager == null)
                return false;
            notificationManager.cancel(Integer.parseInt(notification.getId()));
            if(chargeRegex == null) {
                String regex = "^.+\\d{1,3}%.+$"; // on lg q6 title of notification - Charging (10%)
                chargeRegex = Pattern.compile(regex);
            }
            Matcher matcher = chargeRegex.matcher(notification.getTitle());
            return matcher.matches();
        }
        else
        return false;
    }

    // method try to prevent notification's spam, which post very quickly on ~same time
    // save timestamp and count number ot specific packet posted for 2 sec interval
    // return true if spam, false otherwise
    private boolean checkSpecialNotifs(Notification notification) {
        boolean result = false;
        String pack = notification.getPack() + notification.getTitle();
        long currTime = System.currentTimeMillis();
        AbstractMap.SimpleEntry<Long, Integer> longIntegerSimpleEntry = spamDefender.get(pack);
        Integer count = 0;
        if(longIntegerSimpleEntry != null) {
            Long prevTime = longIntegerSimpleEntry.getKey();
            count = longIntegerSimpleEntry.getValue();
            if (count == null)
                count = 0;
            if (currTime - prevTime >= 2000) {
                spamDefender.remove(pack);
                result = false;
                count = 0;
            } else if (count >= 3) { // if number of that package appearance - 3
                if (currTime - prevTime <= 2000) {  // if 3 time counted only for 2 second's
                    currTime = prevTime;
                    result = true;
                    if (count >= 5) { // too hard spam
                        currTime -= 5000; // we take 5 sec, now 5 more sec ignore this packet
                    }
                }
            }
        }
        if(spamDefender.size() >= 19) // actually 20, after you put another value
            spamDefender.clear();
        count++;
        AbstractMap.SimpleEntry<Long,Integer> store = new AbstractMap.SimpleEntry<Long,Integer>(currTime,count);
        spamDefender.put(pack,store);
        return result;
    }




}
