package com.example.buhalo.lazyir;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DragableButton;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.service.BackgroundService;

/**
 * Created by buhalo on 21.02.17.
 */

public class AppBoot extends Application {

    final String LOG_TAG = "AppBoot";

    ServiceConnection sConn;
    Intent intent;
    public static BackgroundService backgroundService;
    boolean bound = false;

    @Override
    public void onCreate() {

        super.onCreate();
        intent = new Intent(this, BackgroundService.class);
        sConn = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "onServiceConnected");
                backgroundService = ((BackgroundService.BackgroundBinder) binder).getService();
                bound = true;

                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);

                android.net.NetworkInfo wifi = connMgr.getActiveNetworkInfo();
                if (wifi != null && wifi.getType() == ConnectivityManager.TYPE_WIFI && wifi.isAvailable())
                {
                    backgroundService.startListeningUdp(5667);
                    backgroundService.startSendingPeriodicallyUdp(5667);
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "onServiceDisconnected");
                bound = false;
            }
        };

        bindService(intent, sConn,  BIND_AUTO_CREATE);

    }
}
