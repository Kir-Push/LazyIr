package com.example.buhalo.lazyir.service;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.notificationModule.Messengers;
import com.example.buhalo.lazyir.modules.notificationModule.Notification;
import com.example.buhalo.lazyir.modules.notificationModule.Notifications;

import java.util.List;

/**
 * Created by buhalo on 21.03.17.
 */

public class NotificationListener extends NotificationListenerService {
    public static NotificationListener notif;
    public static final String SHOW_NOTIFICATION = "ShowNotification";
    public static final String RECEIVE_NOTIFICATION = "receiveNotification";
    public static final String DELETE_NOTOFICATION = "deleteNotification";
    public static final String SMS_TYPE = "com.android.mms";

    //---------------------------------------------
    public static final String SMS_TYPE_2 = "com.android.messaging";

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
        if(notif == null)
            notif = this;

        //first check if notification is not smsMessage, if it is - send as sms
            if(!smsMessage(sbn)){
                // after check if this  is not messenger message, if it is - send as message
        if(!messengersMessage(sbn))
            // if previous two false, this is notif, send to server
                sendToserver(sbn, RECEIVE_NOTIFICATION);}
        } catch (Exception e) {
        Log.e("NotificationListener",e.toString());
        }

    }
 // i here add new methods after testing change old to new
    private boolean messengersMessage(StatusBarNotification sbn) {
        Bundle bundle = sbn.getNotification().extras;
        Notification notification = castToMyNotification(sbn);
        if(notification == null || notification.getPack() == null  || notification.getPack().equals("com.google.android.googlequicksearchbox"))
        {
            return true;
        }
        for (String key : bundle.keySet()) {
            if("android.wearable.EXTENSIONS".equals(key)){

                if((sbn.getId() > 1 && notification.getPack().equals("org.telegram.messenger")) || !notification.getPack().equals("org.telegram.messenger")) {// telegram send second notif with id 1, it not contain action, therefore ignore it
                    for (Device device : Device.getConnectedDevices().values()) {
                        Messengers messenger = null;
                        if(device != null && (messenger = (Messengers)device.getEnabledModules().get(Messengers.class.getSimpleName())) != null) {
                            messenger.getPendingNotifsLocal().put(notification.getPack() + ":" + notification.getTitle(), sbn);
                        }
                    }
                    Messengers.sendToServer(notification);
                }
                return true;
            }
        }
        // todo фигня выходит, верни обратно одну статическую pendingNotifs и не парь мозг
        for (Device device : Device.getConnectedDevices().values()) {
            Messengers messenger = null;
            if(device != null && (messenger = (Messengers)device.getEnabledModules().get(Messengers.class.getSimpleName())) != null && messenger.getPendingNotifsLocal().containsKey(notification.getPack()+":"+notification.getTitle())) {
                Messengers.sendToServer(notification);
                return true;
            }
        }
        return false;
    }

    //check if notification is SmsMessage or not
    private boolean smsMessage(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();
        return pack.equals(SMS_TYPE) || pack.equals(SMS_TYPE_2);
    }
    public static StatusBarNotification[] getAll()
    {
      return   notif == null ? null : notif.getActiveNotifications();
    }

    public Notification castToMyNotification(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();
        if(pack.equals(SMS_TYPE))
            return null;
        String text = "";
        String title;
        String ticker = "";
        Bundle extras = sbn.getNotification().extras;
        String txt = tryExtract(extras);
        CharSequence charSequence = extras.getCharSequence("android.text");
        CharSequence tickerText = sbn.getNotification().tickerText;
        if(charSequence != null)
        {
            text = charSequence.toString();
        }
        if(txt != null)
        {
            text = txt;
        }
        if(tickerText != null)
        {
            ticker = tickerText.toString();
        }
        title = extras.getString("android.title");
        if(title == null)
        {
            CharSequence bigText = (CharSequence) extras.getCharSequence(android.app.Notification.EXTRA_TEXT);
            if(bigText != null)
                title = bigText.toString();
        }
        if(pack.equals("com.whatsapp") && txt == null)
        {
            return null;
        }
        // telegram send two notifs, first notif with new message title contain action, second not. Skip if new message non exist
//        if(pack.equals("org.telegram.messenger") && !title.contains("(\d new message)"))
        Notification notification = new Notification(text,title,pack,ticker, android.os.Build.MODEL);
        Log.i(SHOW_NOTIFICATION,pack + "  " + title + "  " + text + "  " + ticker);
        return notification;
    }

    private String tryExtract(Bundle extras)
    {
        CharSequence[] lines =
                extras.getCharSequenceArray(android.app.Notification.EXTRA_TEXT_LINES);
        if(lines != null && lines.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (CharSequence msg : lines)
                if (!TextUtils.isEmpty(msg)) {
                    sb.append(msg.toString());
                    sb.append('\n');
                }
            return sb.toString().trim();
        }
        CharSequence chars =
                extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT);
        if(!TextUtils.isEmpty(chars))
            return chars.toString();
        return null;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        try {
            Notification notification = castToMyNotification(sbn);
            if (notification != null && notification.getPack() != null) {
                for (Device device : Device.getConnectedDevices().values()) {
                    Messengers messenger = null;
                    if (device != null && (messenger = (Messengers) device.getEnabledModules().get(Messengers.class.getSimpleName())) != null && messenger.getPendingNotifsLocal().containsKey(notification.getPack() + ":" + notification.getTitle())) {
                        messenger.getPendingNotifsLocal().remove(notification.getPack() + ":" + notification.getTitle());
                    }
                }
            }
           // sendToserver(sbn, DELETE_NOTOFICATION);
            Log.i("Msg", "Notification was removed");
        }catch (Exception e)
        {
            Log.e("NotificationListener",e.toString());
        }
    }

    private void sendToserver(StatusBarNotification sbn,String method)
    {
        try {
            if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.KITKAT) {
                return;
            }
            NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION,method);
            Notification notification = castToMyNotification(sbn);
            if(notification == null)
                return;
           np.setValue("title",notification.getTitle());
            np.setValue("text",notification.getText());
            np.setValue("pack",notification.getPack());
            np.setValue("ticker",notification.getTicker());
            String message = np.getMessage();
            if(message != null && !message.equals(""))
            TcpConnectionManager.getInstance().sendCommandToAll(np.getMessage());
        }catch (Exception e)
        {
            Log.e(SHOW_NOTIFICATION,e.toString());
        }
    }

}
