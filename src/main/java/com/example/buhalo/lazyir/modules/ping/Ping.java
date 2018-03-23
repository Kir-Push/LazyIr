package com.example.buhalo.lazyir.modules.ping;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.NotifActionReceiver;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Ping extends Module {

    private static ReentrantLock staticLock = new ReentrantLock();
    private static volatile boolean ringing = false;
    private static  MediaPlayer mMediaPlayer;

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data){
            case "Ping":
                ping();
                break;
            default:
                break;
        }
    }

    private void ping() {
        staticLock.lock();
        try{
            if(ringing)
                return;
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context,"3456")
                            .setSmallIcon(R.mipmap.up_icon)
                            .setContentTitle("FOUND ME")
                            .setContentText("IF YOU READ THIS, CLICK THE BUTTON");
            Intent yesAction = new Intent(context,NotifActionReceiver.class);
            yesAction.setAction("TURN OFF");
            yesAction.putExtra("id",device.getId());
            PendingIntent pendingIntentOff= PendingIntent.getBroadcast(BackgroundService.getAppContext(), 775533, yesAction, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.addAction(R.drawable.delete48,"TURN OFF",pendingIntentOff);
            Uri alarmSound = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_ALARM);
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(context, alarmSound);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            try {
                mMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();
            mBuilder.setSound(alarmSound);
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(6661666, mBuilder.build());
            ringing = true;
        }finally {
            staticLock.unlock();
        }

    }


    public static void stopAlarm() {
        staticLock.lock();
        try {
            if (mMediaPlayer != null)
                mMediaPlayer.stop();
            mMediaPlayer = null;
            ringing = false;
        }finally {
            staticLock.unlock();
        }
    }
}
