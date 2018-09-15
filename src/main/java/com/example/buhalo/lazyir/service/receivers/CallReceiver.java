package com.example.buhalo.lazyir.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.example.buhalo.lazyir.modules.notification.CallSmsUtils;
import com.example.buhalo.lazyir.modules.notification.NotificationTypes;
import com.example.buhalo.lazyir.modules.notification.call.CallModuleDto;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

import static com.example.buhalo.lazyir.modules.notification.call.CallModule.api.ANSWER;
import static com.example.buhalo.lazyir.modules.notification.call.CallModule.api.ENDCALL;
import static com.example.buhalo.lazyir.service.BackgroundUtil.checkWifiOnAndConnected;

public class CallReceiver extends BroadcastReceiver {

    private static int ringerMode = -1;
    @Getter @Setter
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    @Getter @Setter
    private static boolean isIncoming;
    @Getter @Setter
    private static String savedNumber;
    @Inject @Getter @Setter
    CallSmsUtils utils;

    @Synchronized
    public static void setRingerMode(int ringer){
        ringerMode = ringer;
    }

    @Synchronized
    public static int getRingerMode(){
        return ringerMode;
    }

    @Synchronized
    private void returnRingerMode(Context context){
        if (getRingerMode() != -1) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setRingerMode(-1);
                setRingerMode(-1);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this,context);
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        if(checkWifiOnAndConnected(context)
                && action != null && extras != null
                && TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {

            BackgroundUtil.addCommand(BackgroundServiceCmds.CACHE_CONNECT,context);
            String stateStr = extras.getString(TelephonyManager.EXTRA_STATE);
            String number = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if (stateStr != null) {
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
            }
            if(!BackgroundUtil.hasActualConnection()){
                int finalState = state;
                BackgroundUtil.getTimerExecutor().schedule(()->  onCallStateChanged(context, finalState, number),700, TimeUnit.MILLISECONDS);
            } else {
                onCallStateChanged(context, state, number);
            }
        }
    }


    private void onCallStateChanged(Context context, int state, String number) {
        if(getLastState() == state){
            return; //No change, debounce extras
        }

        if(state == TelephonyManager.CALL_STATE_RINGING){
            setIncoming(true);
            setSavedNumber(number);
            onIncomingCallReceived(context, number, NotificationTypes.INCOMING.name());
        } else if(state == TelephonyManager.CALL_STATE_OFFHOOK){
            if (getLastState() != TelephonyManager.CALL_STATE_RINGING) {
                setIncoming(false);
                onIncomingCallReceived(context, number, NotificationTypes.OUTGOING.name());
            } else {
                if(isIncoming()){
                    setIncoming(true);
                    onAnswered(context,number, NotificationTypes.ANSWER.name());
                }else{
                    setIncoming(false);
                    onIncomingCallEnded(context, getSavedNumber(),NotificationTypes.OUTGOING.name());
                }
            }
        }else if(state == TelephonyManager.CALL_STATE_IDLE){
            if(getLastState() == TelephonyManager.CALL_STATE_RINGING){
                onIncomingCallEnded(context, getSavedNumber(),NotificationTypes.MISSED_IN.name());
            }
            else if(isIncoming()){
                onIncomingCallEnded(context, getSavedNumber(),NotificationTypes.INCOMING.name());
            }
            else{
                onIncomingCallEnded(context, getSavedNumber(),NotificationTypes.OUTGOING.name());
            }
        }
        setLastState(state);
    }

    private void ringerModeReturn(Context context) {
        returnRingerMode(context);
    }

    private void onAnswered(Context context, String number, String type) {
        CallModuleDto dto = new CallModuleDto(ANSWER.name(), type,  utils.getName(number,context.getApplicationContext()));
        EventBus.getDefault().post(dto);
    }

    private void onIncomingCallEnded(Context context, String savedNumber,String type) {
        ringerModeReturn(context);
        CallModuleDto dto = new CallModuleDto(ENDCALL.name(), type,  utils.getName(savedNumber,context.getApplicationContext()));
        EventBus.getDefault().post(dto);
    }

    private void onIncomingCallReceived(Context context, String number,String type) {
        CallModuleDto dto = new CallModuleDto(ENDCALL.name(), type,  utils.getName(number,context.getApplicationContext()));
        if(type.equals(NotificationTypes.OUTGOING.name())) {
            dto.setText("Outgoing CALL");
        } else {
            dto.setText("Incoming CALL");
        }
        dto.setIcon(utils.getContactImage(context,number));
        EventBus.getDefault().post(dto);
    }
}
