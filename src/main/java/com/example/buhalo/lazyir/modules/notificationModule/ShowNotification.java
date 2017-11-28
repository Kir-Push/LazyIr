package com.example.buhalo.lazyir.modules.notificationModule;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.io.ByteArrayOutputStream;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

/**
 * Created by buhalo on 20.04.17.
 */

public class ShowNotification extends Module {
    public static final String SHOW_NOTIFICATION = "ShowNotification";

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals("ALL NOTIFS")) {
            Notifications notifications = new Notifications();
//            String ns = Context.NOTIFICATION_SERVICE;
            StatusBarNotification[] activeNotifications =NotificationListener.getAll();
            if(activeNotifications == null) {
                return;
            }
            for (StatusBarNotification activeNotification : activeNotifications) {
                notifications.addNotification(NotificationUtils.castToMyNotification(activeNotification));
            }
            NetworkPackage nps = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION,"ALL NOTIFS");
            nps.setObject(NetworkPackage.N_OBJECT,notifications);
            sendMsg(nps.getMessage());
        }
    }

    @Override
    public void endWork() {

    }


}
