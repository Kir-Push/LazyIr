package com.example.buhalo.lazyir.modules.notificationModule.notifications;

import android.service.notification.StatusBarNotification;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationListener;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.Notifications;

/**
 * Created by buhalo on 20.04.17.
 */

public class ShowNotification extends Module {
    public static final String SHOW_NOTIFICATION = "ShowNotification";
    public static final String ALL_NOTIFICATIONS = "ALL NOTIFS";
    public static final String REMOVE_NOTIFICATION = "RemoveNotification";

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(ALL_NOTIFICATIONS)) {
            Notifications notifications = getAllNotifications();
            if(notifications != null) {
                NetworkPackage nps = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION, "ALL NOTIFS");
                nps.setObject(NetworkPackage.N_OBJECT, notifications);
                sendMsg(nps.getMessage());
            }
        }else if(np.getData().equals(REMOVE_NOTIFICATION)){
            //todo
        }
    }

    private Notifications getAllNotifications(){
        Notifications notifications = new Notifications();
        StatusBarNotification[] activeNotifications = NotificationListener.getAll();
        if(activeNotifications == null) {
            return null;
        }
        for (StatusBarNotification activeNotification : activeNotifications) {
            notifications.addNotification(NotificationUtils.castToMyNotification(activeNotification));
        }
        return notifications;
    }

    @Override
    public void endWork() {

    }


}
