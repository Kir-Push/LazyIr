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
import android.util.Log;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.service.receivers.NotifActionReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import lombok.Synchronized;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.buhalo.lazyir.modules.ping.Ping.api.STOP;

public class Ping extends Module {
    private static final String TAG = "Ping";
    public enum api{
        PING,
        STOP
    }
    private static boolean ringing;
    private static MediaPlayer mMediaPlayer;
    private static ScheduledFuture<?> schedule;
    private static final int ID = 6661666;
    private static final String CHANNEL_ID = "56756456";

    @Inject
    public Ping(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void execute(NetworkPackage np) {
        PingDto dto = (PingDto) np.getData();
        if(dto.getCommand().equals(api.PING.name())){
            if(ringing) {
                stopAlarm(context);
            }else{
                ping(context);
            }
        }
    }


    @Synchronized
    private static void ping(Context context) {
        if (ringing) {
            stopAlarm(context);
        }
        Intent stopAlarmAction = new Intent(context, NotifActionReceiver.class);
        stopAlarmAction.setAction("lazyIr-cmd");
        stopAlarmAction.putExtra("action", "stopAlarm");
        stopAlarmAction.putExtra("id", ID);
        PendingIntent pendingIntentOff = PendingIntent.getBroadcast(context, 775533, stopAlarmAction, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.up_icon)
                .setContentTitle("FOUND ME")
                .setContentText("Click to turn Off")
                .setContentIntent(pendingIntentOff)
                .setAutoCancel(true);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alarmSound);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            Log.e(TAG, "error in ping", e);
            stopAlarm(context);
            return;
        }
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();
        mBuilder.setSound(alarmSound);
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (mNotifyMgr == null) {
            stopAlarm(context);
            return;
        }
        mNotifyMgr.notify(ID, mBuilder.build());
        ringing = true;
        schedule = BackgroundUtil.getTimerExecutor().schedule(() -> stopAlarm(context), 30, TimeUnit.SECONDS);
    }


    @Synchronized
    private static void stopAlarm(Context context) {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        if(schedule != null){
            schedule.cancel(true);
        }
        schedule = null;
        mMediaPlayer = null;
        ringing = false;
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if(mNotifyMgr != null) {
            mNotifyMgr.cancel(ID);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void receiveCommand(PingDto dto){
        if(ringing && dto.getCommand().equals(STOP.name())){
            stopAlarm(context);
        }
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
        if(BackgroundUtil.ifLastConnectedDeviceAreYou(device.getId())){
            stopAlarm(context);
        }
    }
}
