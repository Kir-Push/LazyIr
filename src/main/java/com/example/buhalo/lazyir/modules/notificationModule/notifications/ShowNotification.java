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
            Notifications notifications = new Notifications();
//            String ns = Context.NOTIFICATION_SERVICE;
            StatusBarNotification[] activeNotifications = NotificationListener.getAll();
            if(activeNotifications == null) {
                return;
            }
            for (StatusBarNotification activeNotification : activeNotifications) {
                notifications.addNotification(NotificationUtils.castToMyNotification(activeNotification));
            }
            NetworkPackage nps = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION,"ALL NOTIFS");
            nps.setObject(NetworkPackage.N_OBJECT,notifications);
            sendMsg(nps.getMessage());
        }else if(np.getData().equals(REMOVE_NOTIFICATION)){
            //todo
        }
    }

    @Override
    public void endWork() {

    }


}
