package com.example.buhalo.lazyir.modules.notification.messengers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.annimon.stream.Objects;
import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.notification.notifications.Notification;
import com.example.buhalo.lazyir.modules.notification.NotificationUtils;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import static com.example.buhalo.lazyir.modules.notification.messengers.Messengers.api.ANSWER;


public class Messengers extends Module {
    private static final String TAG = "Messengers";
    public enum api{
        ANSWER
    }

    private NotificationUtils utils;

    @Inject
    public Messengers(MessageFactory messageFactory,NotificationUtils utils, Context context) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
        this.utils = utils;
    }

    @Override
    public void execute(NetworkPackage np) {
        MessengersDto dto = (MessengersDto) np.getData();
        if(dto.getCommand().equals(ANSWER.name())) {
            answer(dto);
        }
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
        if(BackgroundUtil.ifLastConnectedDeviceAreYou(device.getId())){
            utils.getPendingNotifs().clear();
        }
    }

    private void answer(MessengersDto dto) {
            String whom = dto.getTypeName();
            String text = dto.getText();
            StatusBarNotification statusBarNotification = utils.getPendingNotifs().get(whom);
            if(statusBarNotification != null) {
                answerMessenger(statusBarNotification, text);
                utils.getPendingNotifs().remove(whom);
            }
    }

    private void answerMessenger(StatusBarNotification sbn,String answer) {
        android.app.Notification notification = sbn.getNotification();
        if(notification == null){
            return;
        }
        Bundle bundle = notification.extras;
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(notification);
        Stream.of( wearableExtender.getActions()).filter(Objects::nonNull).filter(act -> act.getRemoteInputs() != null)
                .forEach((NotificationCompat.Action action) -> {
                    Intent localIntent = new Intent();
                    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    RemoteInput[] remoteInputs = action.getRemoteInputs();
                    for (RemoteInput remoteInput : remoteInputs) {
                        try {
                        bundle.putCharSequence(remoteInput.getResultKey(),answer);
                        RemoteInput.addResultsToIntent(remoteInputs,localIntent,bundle);
                        action.actionIntent.send(context.getApplicationContext(), 0, localIntent);
                        } catch (PendingIntent.CanceledException e) {
                            Log.e(TAG,"error in answerMessenger",e);
                        }
                    }
                });
    }


     private void sendToServer(Notification notification) {
         String typeName = notification.getPack() + ":" + notification.getTitle();
         MessengersDto dto = new MessengersDto(api.ANSWER.name());
         dto.setTypeName(typeName);
         dto.setNotification(notification);
         notification.setPack(notification.getPack());
         String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, dto);
         sendMsg(message);
    }

    private String tryExtractPack(String pack) {
        int i;
       return (i = pack.lastIndexOf('.')) != -1 ? pack.substring(i) : pack;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void receiveMessage(MessengersDto dto){
            sendToServer(dto.getNotification());
    }
}
