package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.Exception.TcpError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 21.03.17.
 */

public class NotificationListener extends NotificationListenerService {
    Context context;

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) { // todo all of this only for test after you need create normal version !


        try {


            String pack = sbn.getPackageName();
            String text = "";
            String title = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                Bundle extras = extras = sbn.getNotification().extras;
                text = extras.getCharSequence("android.text").toString(); // todo null pointer reference
                title = extras.getString("android.title");
            }
            List<String> args = new ArrayList();
            args.add(title);
            args.add(text);
            NetworkPackage np = new NetworkPackage();
            np.setArgs(args);
            String fromTypeAndData = np.createFromTypeAndData("ShowNotification", "receiveNotification");
            for (Device dv : Device.getConnectedDevices().values()) {
                try {
                    TcpConnectionManager.getInstance().sendCommandToServer(dv.getId(), fromTypeAndData);
                } catch (TcpError tcpError) {
                    tcpError.printStackTrace();
                }
            }
            Log.i("Package", pack);
            Log.i("Title", title);
            Log.i("Text", text);
       //     Log.i("Ticker", sbn.getNotification().tickerText.toString());
            for (StatusBarNotification statusBarNotification : getActiveNotifications()) {
                Log.i("Notificiation", statusBarNotification.getNotification().extras.getCharSequence("android.text").toString());
                System.out.println(statusBarNotification.getNotification().tickerText);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg","Notification was removed");
    }
}
