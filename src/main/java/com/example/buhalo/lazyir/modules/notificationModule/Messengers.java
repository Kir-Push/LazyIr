package com.example.buhalo.lazyir.modules.notificationModule;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 18.04.17.
 */

public class Messengers extends Module {

    private static final String ANSWER = "answer";
    // you may receive answer from many device simultaneously, you need work with pengnotifs one by one, if 2 device answer for one message,
    // you get one, answer, remove, and second answer will be skipped
    private static Lock lock = new ReentrantLock();
    public static ConcurrentHashMap<String,StatusBarNotification> pendingNotifs = new ConcurrentHashMap<>();


    //todo this is testing variant for (you need to test, can you use some pending StatusBarNotification many times, if you can,
    // todo use it many times, if many device answer simultaneously.)
    // todo you will need create getter and setter for that and each device will have independent instance,
    // todo it will consume memory, but for 5-10 device not much,
    // todo remember TEST FOR MULTIPLE USE STATUSBARNOTIFICATION 
    private ConcurrentHashMap<String,StatusBarNotification> pendingNotifsLocal = new ConcurrentHashMap<>();

    private static boolean taskStarted = false;

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(ANSWER)) {
            answer(np);
        }
    }

    @Override
    public void endWork() {
        if(Device.getConnectedDevices().size() == 0)
            pendingNotifs.clear();
    }

    // todo you need to test, can you use some pending StatusBarNotification many times, if you can,
    // todo use it many times, if many device answer simultaneously.
    private void answer(NetworkPackage np) {
        lock.lock();
        try {
            String whom = np.getValue("typeName");
            String text = np.getValue("text");
            StatusBarNotification statusBarNotification = pendingNotifs.get(whom);
            answerMessenger(statusBarNotification, text);
            pendingNotifs.remove(whom);
        }finally {
            lock.unlock();
        }
    }

    private void answerMessenger(StatusBarNotification sbn,String answer) {
        Bundle bundle = sbn.getNotification().extras;

                NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(sbn.getNotification());
                List<NotificationCompat.Action> actions = wearableExtender.getActions();
        for(NotificationCompat.Action act : actions) {
                    if(act != null && act.getRemoteInputs() != null) {
                        RemoteInput[] remoteInputs = act.getRemoteInputs();

                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        int i = 0;
                        for(RemoteInput remoteIn : remoteInputs){
                            remoteInputs[i] = remoteIn;
                            remoteInputs[i].getAllowFreeFormInput();
                            bundle.putCharSequence(remoteInputs[i].getResultKey(), answer);//This work, apart from Hangouts as probably they need additional parameter (notification_tag?)
                            RemoteInput.addResultsToIntent(remoteInputs, localIntent, bundle);
                            try {
                                act.actionIntent.send(context.getApplicationContext(),0,localIntent);
                            } catch (PendingIntent.CanceledException e) {
                                Log.e("Msg", "replyToLastNotification error: " + e.getLocalizedMessage());
                            }
                            i++;
                        }
                    }
                }
    }


    public static void sendToServer(Notification notification) {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(Messengers.class.getSimpleName(),ANSWER);
        String typeName = notification.getPack()+":"+notification.getTitle();
        np.setValue("typeName",typeName);
        np.setValue("pack",tryExtractPack(notification.getPack()));
        np.setValue("title",notification.getTitle());
        np.setValue("ticker",notification.getTicker());
        np.setValue("text",notification.getText());
        TcpConnectionManager.getInstance().sendCommandToAll(np.getMessage());
    }

    private static String tryExtractPack(String pack) {
        int i;
       return (i = pack.lastIndexOf(".")) != -1 ? pack.substring(i) : pack;
    }

}
