package com.example.buhalo.lazyir.modules.notificationModule.notifications;

import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.notificationModule.messengers.Messengers;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.util.HashMap;

import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils.messengersMessage;
import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils.smsMessage;

/**
 * Created by buhalo on 21.03.17.
 */

// todo thread problem, it work in main thread, you don't need that,
    //  you need work in separate thread, maybe in backgroundService
    // https://stackoverflow.com/questions/17926236/notificationlistenerservice-implementation
public class NotificationListener extends NotificationListenerService {
    private static NotificationListener notif;
    public static final String SHOW_NOTIFICATION = "ShowNotification";
    public static final String RECEIVE_NOTIFICATION = "receiveNotification";
    public static final String DELETE_NOTOFICATION = "deleteNotification";
    public static final String SMS_TYPE = "com.android.mms";

    //---------------------------------------------
    public static final String SMS_TYPE_2 = "com.android.messaging";

    private static HashMap<String,Long> notifsToFrequent = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        // todo fill notifsToFrequent with notification's name's which you don't have too frequent show (charging);
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

            if(notif == null)
            notif = this;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && sbn.getId() == BackgroundService.NotifId)
            snoozeNotification(sbn.getKey(), 600000);

            if(!smsMessage(sbn)){                        //first check if notification is not smsMessage, if it is - send as sms
                Notification notification = NotificationUtils.castToMyNotification(sbn);
                if(notification != null && messengersMessage(sbn,notification.getPack(),notification.getTitle(),notification.getText())) {
                    if(!notification.getPack().equals("com.google.android.googlequicksearchbox"))
                    Messengers.sendToServer(notification);    // after check if this  is not messenger message, if it is - send as message
                }
                else
                  sendToAll(sbn, RECEIVE_NOTIFICATION);  // if previous two false, this is notif, send to server
            }
        } catch (Throwable e) {                          // i don't need crash app if something going wrong
        Log.e("NotificationListener","onNotificationPosted error",e);
        }

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
            boolean duplicate = checkNotificationForDuplicates(notification);
            if(notification == null || duplicate)
                return;
            np.setObject(NetworkPackage.N_OBJECT,notification);
            String message = np.getMessage();
            if(message != null && !message.equals(""))
               BackgroundService.sendToAllDevices(np.getMessage());
        }catch (Exception e) {
            Log.e(SHOW_NOTIFICATION,e.toString());
        }
    }

    private boolean checkNotificationForDuplicates(Notification notification) {
        String text = notification.getText() + notification.getTitle();
        long currTime = System.currentTimeMillis();
        Long aLong = notifsToFrequent.get(text);
        if (notifsToFrequent.size() >= 20)
            notifsToFrequent.clear();  // don't store too many object's
        notifsToFrequent.put(text, currTime);
        if (aLong == null) {
            return false;
        } else {
            long difference = currTime - aLong;
            return difference < 20000;
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
