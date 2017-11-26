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
import android.util.Base64;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;

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

            if(!smsMessage(sbn)){                        //first check if notification is not smsMessage, if it is - send as sms
                if(!messengersMessage(sbn))              // after check if this  is not messenger message, if it is - send as message
                  sendToAll(sbn, RECEIVE_NOTIFICATION);  // if previous two false, this is notif, send to server
            }
        } catch (Throwable e) {                          // i don't need crash app if something going wrong
        Log.e("NotificationListener","onNotificationPosted error",e);
        }

    }
 // i here add new methods after testing change old to new
    private boolean messengersMessage(StatusBarNotification sbn) {
        Bundle bundle = sbn.getNotification().extras;
        Notification notification = NotificationUtils.castToMyNotification(sbn);
        if(notification == null || notification.getPack() == null  || notification.getPack().equals("com.google.android.googlequicksearchbox"))
            return true;

        for (String key : bundle.keySet()) {
            if("android.wearable.EXTENSIONS".equals(key)){

                if((sbn.getId() > 1 && notification.getPack().equals("org.telegram.messenger")) || !notification.getPack().equals("org.telegram.messenger")) {        // telegram send second notif with id 1, it not contain action, therefore ignore it
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
    private boolean smsMessage(StatusBarNotification sbn) {
        String pack = sbn.getPackageName();
        return pack.equals(SMS_TYPE) || pack.equals(SMS_TYPE_2);
    }
    public static StatusBarNotification[] getAll() {
      return   notif == null ? null : notif.getActiveNotifications();
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        try {
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
            if(notification == null)
                return;
            np.setObject(NetworkPackage.N_OBJECT,notification); // todo change from setValue, you need change in server to correspond!
            String message = np.getMessage();
            if(message != null && !message.equals(""))
               BackgroundService.sendToAllDevices(np.getMessage());
        }catch (Exception e) {
            Log.e(SHOW_NOTIFICATION,e.toString());
        }
    }
    //https://stackoverflow.com/questions/20522133/use-android-graphics-bitmap-from-java-without-android  --- bitmap to img (pixels)
//    private ImageDTO getIcon(StatusBarNotification sbn){
//        android.app.Notification notification = sbn.getNotification();
//        Icon icon = (icon = notification.getLargeIcon()) == null ? notification.getSmallIcon() : icon;
//        ImageDTO imageDTO;
//        if(icon == null)
//            return null;
//        try {
//            Context packageContext = createPackageContext(sbn.getPackageName(), CONTEXT_IGNORE_SECURITY);
//            Drawable drawable = icon.loadDrawable(packageContext);
//            Bitmap bitmap = drawableToBitmap(drawable);
//            int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];
//            bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
//            imageDTO = new ImageDTO(pixels,bitmap.getWidth(),bitmap.getHeight());
//            return imageDTO;
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e("NotificationListener","getIcon",e);
//            return  null;
//        }
//    }



}
