package com.example.buhalo.lazyir.modules.notification.call;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.listeners.NotificationListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;


import static com.example.buhalo.lazyir.service.receivers.CallReceiver.setRingerMode;


public class CallModule extends Module {
    private static final String TAG = "CallModule";
    public enum api{
        CALL,
        ENDCALL,
        ANSWER,
        ANSWER_CALL,
        DECLINE_CALL,
        MUTE,
        RECALL
    }

    @Inject
    public CallModule(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void execute(NetworkPackage np) {
        CallModuleDto dto = (CallModuleDto) np.getData();
        api command = CallModule.api.valueOf(dto.getCommand());
        switch (command) {
            case ANSWER_CALL:
                answerCall(dto);
                break;
            case DECLINE_CALL:
                declineCall(dto);
                break;
            case MUTE:
                muteCall(dto);
                break;
            case RECALL:
                call(dto);
                break;
            default:
                break;
        }
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
    }

    private void answerCall(CallModuleDto dto) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelecomManager tm = (TelecomManager) context
                        .getSystemService(Context.TELECOM_SERVICE);
                if (tm == null) {
                   return;
                }
                tm.acceptRingingCall();
            } else {
                sendHeadsetHook();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "error in answerCall - " + dto.getNumber(), e);
        }
    }

    private void sendHeadsetHook() {
        MediaSessionManager mediaSessionManager =  (MediaSessionManager)  context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        if(mediaSessionManager != null) {
            List<MediaController> mediaControllerList = mediaSessionManager.getActiveSessions(new ComponentName(context, NotificationListener.class));
            for (MediaController m : mediaControllerList) {
                if ("com.android.server.telecom".equals(m.getPackageName())) {
                    m.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                    break;
                }
            }
        }
    }

    private void declineCall(CallModuleDto dto) {
        try {
            TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if(tm == null) {
                return;
            }
            if (tm.isInCall()) {
                phoneCallEnd();
            }
        }catch (SecurityException e){
            Log.e(TAG, "error in declineCall - " + dto.getNumber(), e);
        }
    }

    private void muteCall(CallModuleDto dto) {
        try {
            TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if(tm == null) {
                return;
            }
            if (tm.isInCall()) {
                phoneCallMute();
            }
        }catch (SecurityException e){
            Log.e(TAG, "error in muteCall - " + dto.getNumber(), e);
        }
    }

    private void phoneCallEnd() {
        itelephoneMethodCall("endCall");
    }

    private void phoneCallMute(){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager != null) {
            setRingerMode(audioManager.getRingerMode());
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    private void itelephoneMethodCall(String method){
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if(telephonyManager == null){
                return;
            }
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");
            if(methodGetITelephony == null){
                return;
            }
            methodGetITelephony.setAccessible(true);
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);
            Class telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod(method);
            if(methodEndCall != null) {
                // Invoke endCall()
                methodEndCall.invoke(telephonyInterface);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) { // Many things can go wrong with reflection calls
            Log.e(TAG, "error in itelephoneMethodCall - " + method, e);
        }
    }

    private void call(CallModuleDto dto) {
        //can't do that
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void receiveCallSignal(CallModuleDto dto){
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,dto));
    }


}
