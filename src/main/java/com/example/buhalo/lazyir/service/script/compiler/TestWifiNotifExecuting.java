package com.example.buhalo.lazyir.service.script.compiler;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.NotificationCompat;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.service.JasechBroadcastReceiver;
import com.example.buhalo.lazyir.service.script.actions.BaseAction;
import com.example.buhalo.lazyir.service.script.callback.BroadcastCallback;

import static com.example.buhalo.lazyir.service.JasechBroadcastReceiver.checkWifiOnAndConnected;

/**
 * Created by buhalo on 22.10.17.
 */

public class TestWifiNotifExecuting {

    public void test()
    {
        BroadcastCallback broadcastCallback = new BroadcastCallback();
        BaseAction action = new BaseAction() {
            @Override
            public boolean call(Object... args) {
                return false;
            }

            @Override
            public boolean call(Context context, Intent intent) {
                boolean currCheck = checkWifiOnAndConnected(context);
                System.out.println("WIFI ACTION CALLING!!  " + currCheck);
                if(currCheck)
                {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context);
                    mBuilder
                                    .setSmallIcon(R.drawable.btn_close)
                                    .setContentTitle("My notification")
                                    .setContentText("Hello World!");


// Gets an instance of the NotificationManager service//

                    NotificationManager mNotificationManager =

                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//When you issue multiple notifications about the same type of event, it’s best practice for your app to try to update an existing notification with this new information, rather than immediately creating a new notification. If you want to update this notification at a later date, you need to assign it an ID. You can then use this ID whenever you issue a subsequent notification. If the previous notification is still visible, the system will update this existing notification, rather than create a new one. In this example, the notification’s ID is 001//

                 //   NotificationManager.notify().

                            mNotificationManager.notify(001, mBuilder.build());
                }
                return false;
            }
        };
        BaseAction action2 = new BaseAction() {
            @Override
            public boolean call(Object... args) {
                return false;
            }

            @Override
            public boolean call(Context context, Intent intent) {
                boolean currCheck = checkWifiOnAndConnected(context);
                System.out.println("WIFI ACTION CALLING2!!  " + currCheck);
                if(!currCheck)
                {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context);
                    mBuilder
                            .setSmallIcon(R.drawable.btn_close)
                            .setContentTitle("WIFI CLOSE")
                            .setContentText("GOODBAY World!");


// Gets an instance of the NotificationManager service//

                    NotificationManager mNotificationManager =

                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//When you issue multiple notifications about the same type of event, it’s best practice for your app to try to update an existing notification with this new information, rather than immediately creating a new notification. If you want to update this notification at a later date, you need to assign it an ID. You can then use this ID whenever you issue a subsequent notification. If the previous notification is still visible, the system will update this existing notification, rather than create a new one. In this example, the notification’s ID is 001//

                    //   NotificationManager.notify().

                    mNotificationManager.notify(002, mBuilder.build());
                }
                return false;
            }
        };
        broadcastCallback.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION,action);
        broadcastCallback.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION,action2);
        JasechBroadcastReceiver.BroadcastcallBacks.add(broadcastCallback);
    }
}
