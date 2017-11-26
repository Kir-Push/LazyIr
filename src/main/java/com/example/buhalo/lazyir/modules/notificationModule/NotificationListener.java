package com.example.buhalo.lazyir.modules.notificationModule;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

/**
 * Created by buhalo on 21.03.17.
 */

// todo thread problem, it work in main thread, you don't need that,
    // todo you need work in separate thread, maybe in backgroundService
    //todo https://stackoverflow.com/questions/17926236/notificationlistenerservice-implementation
public class NotificationListener extends NotificationListenerService {
    private static NotificationListener notif;
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

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && sbn.getId() == BackgroundService.NotifId)
            snoozeNotification(sbn.getKey(), 600000);

        //first check if notification is not smsMessage, if it is - send as sms
            if(!smsMessage(sbn)){
                if(!messengersMessage(sbn)) // after check if this  is not messenger message, if it is - send as message
                  sendToserver(sbn, RECEIVE_NOTIFICATION);   // if previous two false, this is notif, send to server
            }
        } catch (Throwable e) { // i don't need crash app if something going wrong
        Log.e("NotificationListener","onNotificationPosted error",e);
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
                    Messengers.getPendingNotifsLocal().put(notification.getPack() + ":" + notification.getTitle(), sbn);
                    Messengers.sendToServer(notification);
                }
                return true;
            }
        }
       if( Messengers.getPendingNotifsLocal().containsKey(notification.getPack()+":"+notification.getTitle())) {
           Messengers.sendToServer(notification);
           return true;
       }
        return false;
    }

    //check if notification is SmsMessage or not
    private boolean smsMessage(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();
//        if(pack.equals(SMS_TYPE) || pack.equals(SMS_TYPE_2))
//            snoozeNotification(sbn.getKey(),30000);
//            sbn.getNotification().
        return pack.equals(SMS_TYPE) || pack.equals(SMS_TYPE_2);
    }
    public static StatusBarNotification[] getAll()
    {
      return   notif == null ? null : notif.getActiveNotifications();
    }

    // todo reverse notifs for image and text carefully
    public Notification castToMyNotification(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();
        if(smsMessage(sbn))
            return null;
        String text = "";
        String title;
        String ticker = "";
        Bundle extras = sbn.getNotification().extras;
        String txt = tryExtract(extras);
        CharSequence charSequence = extras.getCharSequence("android.text");
        CharSequence tickerText = sbn.getNotification().tickerText;
        if(charSequence != null)
            text = charSequence.toString();
        if(txt != null)
            text = txt;
        if(tickerText != null)
            ticker = tickerText.toString();
        title = extras.getString("android.title");
        if(title == null) {
            CharSequence bigText = (CharSequence) extras.getCharSequence(android.app.Notification.EXTRA_TEXT);
            if(bigText != null)
                title = bigText.toString();
        }
        if(pack.equals("com.whatsapp") && txt == null)
            return null;
        // telegram send two notifs, first notif with new message title contain action, second not. Skip if new message non exist
//        if(pack.equals("org.telegram.messenger") && !title.contains("(\d new message)"))
        Notification notification = new Notification(text,title,pack,ticker, android.os.Build.MODEL);
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
            BackgroundService.sendToAllDevices(np.getMessage());
        }catch (Exception e)
        {
            Log.e(SHOW_NOTIFICATION,e.toString());
        }
    }
    //https://stackoverflow.com/questions/20522133/use-android-graphics-bitmap-from-java-without-android  --- bitmap to img (pixels)
    private int[] getIcon(StatusBarNotification sbn){
        android.app.Notification notification = sbn.getNotification();
        Icon icon = notification.getLargeIcon() == null ? notification.getSmallIcon() : notification.getLargeIcon();
        if(icon == null)
            return new int[0];
        try {
            Context packageContext = createPackageContext(sbn.getPackageName(), CONTEXT_IGNORE_SECURITY);
            Drawable drawable = icon.loadDrawable(packageContext);
            Bitmap bitmap = drawableToBitmap(drawable);
            int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];
            bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
            return pixels;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("NotificationListener","getIcon",e);
        }
        return //todo don't return array, return some wrapper, with int, width,height and size
    }

    //https://stackoverflow.com/questions/37252119/how-to-convert-a-icon-to-bitmap-in-android    --- drawable to bitmap
    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        return bitmap;

}
