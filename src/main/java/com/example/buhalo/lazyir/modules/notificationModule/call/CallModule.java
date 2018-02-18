package com.example.buhalo.lazyir.modules.notificationModule.call;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationListener;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 03.12.17.
 */
//todo
public class CallModule extends Module {
    public static final String ANSWER_CALL = "asnwerCall";
    public static final String DECLINE_CALL = "declineCall";
    public static final String MUTE = "mute";
    public static final String MUTE_NOVIBRO = "muteNoVibro";
    public static final String RECALL = "call";
    private volatile static int Ringer_Mode = -1;
    private static Lock staticLock = new ReentrantLock();

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data) {
            case ANSWER_CALL:
                answerCall(np);
                break;
            case DECLINE_CALL:
                declineCall(np);
                break;
            case MUTE:
                muteCall(np);
                break;
            case MUTE_NOVIBRO:
                muteCall(np);
                noVibro(np);
                break;
            case RECALL:
                call(np);
                break;
            default:
                break;
        }
    }

    private void call(NetworkPackage np) {


    }

    private void noVibro(NetworkPackage np) {

    }

    private void muteCall(NetworkPackage np) {
        try {
            TelecomManager tm = (TelecomManager) BackgroundService.getAppContext()
                    .getSystemService(Context.TELECOM_SERVICE);
            if(tm == null)
                return;
            if (ActivityCompat.checkSelfPermission(BackgroundService.getAppContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (tm.isInCall()) {
                PhoneCallMute();
            }
        }catch (Exception e){

        }
    }

    private void declineCall(NetworkPackage np) {
        try {
            TelecomManager tm = (TelecomManager) BackgroundService.getAppContext()
                    .getSystemService(Context.TELECOM_SERVICE);
            if(tm == null)
                return;
            if (ActivityCompat.checkSelfPermission(BackgroundService.getAppContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (tm.isInCall()) {
                PhoneCallEnd();
            }
        }catch (Exception e){

        }
    }

    void sendHeadsetHook() {
        MediaSessionManager mediaSessionManager =  (MediaSessionManager)  BackgroundService.getAppContext().getSystemService(Context.MEDIA_SESSION_SERVICE);

        try {
            List<MediaController> mediaControllerList = mediaSessionManager.getActiveSessions
                    (new ComponentName(BackgroundService.getAppContext(), NotificationListener.class));

            for (MediaController m : mediaControllerList) {
                if ("com.android.server.telecom".equals(m.getPackageName())) {
                    m.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                    break;
                }
            }
        } catch (SecurityException e) {
        }
    }

    public void PhoneCallEnd() {
       ItelephoneMethodCall("endCall");

    }

    public void PhoneCallMute(){
        AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        try {
            setRinger_Mode(audioManager.getRingerMode());
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRinger_Mode(int ringer){
        staticLock.lock();
        try{
            Ringer_Mode = ringer;
        }finally {
            staticLock.unlock();
        }
    }

    public static void returnRingerMode(){
        staticLock.lock();
        try{
            int ringer_mode = getRinger_Mode();
            if(ringer_mode != -1){
                AudioManager audioManager =
                        (AudioManager) BackgroundService.getAppContext().getSystemService(Context.AUDIO_SERVICE);
                if(audioManager != null) {
                    audioManager.setRingerMode(ringer_mode);
                    CallModule.setRinger_Mode(-1);
                }
            }
        }finally {
            staticLock.unlock();
        }
    }

    public static int getRinger_Mode(){
        staticLock.lock();
        try{
            return Ringer_Mode;
        }finally {
            staticLock.unlock();
        }
    }

    private void ItelephoneMethodCall(String method){
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);

            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());


            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod(method);

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);

        } catch (Exception ex) { // Many things can go wrong with reflection calls

            String error=ex.toString();


        }
    }

    private void answerCall(NetworkPackage np) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            TelecomManager tm = (TelecomManager) BackgroundService.getAppContext()
                    .getSystemService(Context.TELECOM_SERVICE);

            if (tm == null) {// whether you want to handle this is up to you really
                throw new NullPointerException("tm == null");
            }
            tm.acceptRingingCall();
        }else{
            try {
                sendHeadsetHook();
            } catch (Exception e) {

            }
        }

    }
}
