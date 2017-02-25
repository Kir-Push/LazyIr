package com.example.buhalo.lazyir.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by buhalo on 20.02.17.
 */

public class TestServie extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("ja tut");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true)
//                {
//                    System.out.println("yeap yeye!!!");
//                    try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
