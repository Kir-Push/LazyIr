package com.example.buhalo.lazyir.service;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.notificationModule.SmsModule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.example.buhalo.lazyir.modules.notificationModule.CallSmsUtils.getName;
import static com.example.buhalo.lazyir.modules.notificationModule.NotificationListener.SHOW_NOTIFICATION;

/**
 * Created by buhalo on 14.03.17.
 */
// todo separate broadcast for sms and call's, register only if module enabled
public class BaseBroadcastReceiver extends BroadcastReceiver {

    private static boolean lastCheck;
    private static boolean firstTime = true;
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming;
    private static String savedNumber;


    // todo handle mms
    @Override
    public void onReceive(Context context, Intent intent) {

        if(BackgroundService.getAppContext() == null) // todo better sync
            BackgroundService.setAppContext(context.getApplicationContext());
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
                return;
            }
            boolean currCheck = checkWifiOnAndConnected(context);
            if (currCheck == lastCheck && !firstTime) {
                return;
            }
            firstTime = false;
            lastCheck = currCheck;

            Log.d("BroadcastReceiver", "Some action " + action);
            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                if (currCheck) {
                    Log.d("BroadcastReceiver", "WIFI CONNECTED");
                    BackgroundService.addCommandToQueue(BackgroundServiceCmds.startTasks);
                } else {
                    Log.d("BroadcastReceiver", "WIFI NOT CONNECTED");
                    BackgroundService.addCommandToQueue(BackgroundServiceCmds.destroy);
                }
            }
    }

    private void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                savedNumber = number;
                onIncomingCallReceived(context, number,"incoming");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    onIncomingCallReceived(context, number,"outgoing"); // test
                   // onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                else if(isIncoming)
                {
                    isIncoming = true;
                }
                else
                {
                    isIncoming = false;
                    onIncomingCallEnded(context, savedNumber,"outgoing");
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onIncomingCallEnded(context, savedNumber,"missedIn");
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
        if(message != null && !message.equals(""))
        {
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
        if(message != null && !message.equals(""))
        {
            BackgroundService.sendToAllDevices(np.getMessage());
        }
    }


    public static boolean checkWifiOnAndConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr != null && wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            return !(wifiInfo == null || wifiInfo.getNetworkId() == -1);
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }








}
