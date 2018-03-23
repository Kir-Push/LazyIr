package com.example.buhalo.lazyir.modules.notificationModule.notifications;

import android.app.NotificationManager;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.notificationModule.messengers.Messengers;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationListener;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.Notifications;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by buhalo on 20.04.17.
 */

public class ShowNotification extends Module {
    public static final String ALL_NOTIFICATIONS = "ALL NOTIFS";
    public static final String SHOW_NOTIFICATION = "ShowNotification";
    public static final String RECEIVE_NOTIFICATION = "deleteNotification";
    public static final String REMOVE_NOTIFICATION = "removeNotification";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(ALL_NOTIFICATIONS)) {
            Notifications notifications = getAllNotifications();
            if(notifications != null) {
                NetworkPackage nps = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION, "ALL NOTIFS");
                nps.setObject(NetworkPackage.N_OBJECT, notifications);
                String message = nps.getMessage();
                sendMsg(message);
            }
        }else if(np.getData().equals(REMOVE_NOTIFICATION)){
            String value = np.getValue(NOTIFICATION_ID);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context,"someLittleHack")
                            .setSmallIcon(R.mipmap.up_icon)
                            .setContentTitle(value)
                            .setContentText("I should disappear fast, if no something went wrong");
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(999888777, mBuilder.build());
        }
    }

    private Notifications getAllNotifications(){
        Notifications notifications = new Notifications();
        StatusBarNotification[] activeNotifications = NotificationListener.getAll();
        if(activeNotifications == null) {
            return null;
        }
        for (StatusBarNotification activeNotification : activeNotifications) {
            boolean sms = NotificationUtils.smsMessage(activeNotification);
            try {
                if (sms) {
                    Messengers.getPendingNotifsLocal().put(activeNotification.getPackageName() + ":" + NotificationUtils.extractTitle(activeNotification), activeNotification);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            notifications.addNotification(NotificationUtils.castToMyNotification(activeNotification));
        }
        return notifications;
    }

    @Override
    public void endWork() {

    }


}
