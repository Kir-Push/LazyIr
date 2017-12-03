package com.example.buhalo.lazyir.modules.notificationModule.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundService;

import static com.example.buhalo.lazyir.modules.notificationModule.CallSmsUtils.getName;
import static com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationListener.SHOW_NOTIFICATION;

/**
 * Created by buhalo on 03.12.17.
 */

public class CallListener extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if(action == null)
            return;

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            Bundle extras = intent.getExtras();
            if(extras == null)
                return;
            String stateStr = extras.getString(TelephonyManager.EXTRA_STATE);
            String number = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr != null){
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if ( stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
            }
            onCallStateChanged(context, state, number);
        }

    }


    private void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            return; //No change, debounce extras
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                savedNumber = number;
                onIncomingCallReceived(context, number,"incoming");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if(lastState != TelephonyManager.CALL_STATE_RINGING){ //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                    isIncoming = false;
                    onIncomingCallReceived(context, number,"outgoing"); // test   // onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                else if(isIncoming) {
                    isIncoming = true;
                }
                else {
                    isIncoming = false;
                    onIncomingCallEnded(context, savedNumber,"outgoing");
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                if(lastState == TelephonyManager.CALL_STATE_RINGING){ //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    onIncomingCallEnded(context, savedNumber,"missedIn");       //Ring but no pickup-  a miss
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, savedNumber,"incoming");
                }
                else{
                    onIncomingCallEnded(context, savedNumber,"outgoing");
                }
                break;
        }
        lastState = state;
    }

    private void onIncomingCallEnded(Context context, String savedNumber,String type) {
        String name = getName(savedNumber,context.getApplicationContext());
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION,"com.android.endCall");
        np.setValue("number",name);
        np.setValue("callType",type);
        String message = np.getMessage();
        if(message != null && !message.equals("")) {
            BackgroundService.sendToAllDevices(np.getMessage());
        }
    }

    private void onIncomingCallReceived(Context context, String number,String type) {
        String name = getName(number,context.getApplicationContext());
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION,"com.android.call");
        np.setValue("number",name);
        np.setValue("text","Incoming CALL");
        np.setValue("callType",type);
        String message = np.getMessage();
        if(message != null && !message.equals("")) {
            BackgroundService.sendToAllDevices(np.getMessage());
        }
    }
}
