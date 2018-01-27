package com.example.buhalo.lazyir.modules.notificationModule.call;

import android.content.Context;
import android.os.Build;
import android.telecom.TelecomManager;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;

/**
 * Created by buhalo on 03.12.17.
 */
//todo
public class CallModule extends Module {
    public static final String ANSWER_CALL = "asnwerCall";
    public static final String DECLINE_CALL = "declineCall";
    public static final String MUTE = "mute";
    public static final String MUTE_NOVIBRO = "muteNoVibro";
    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data){
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
            default:
                break;
        }
    }

    private void noVibro(NetworkPackage np) {

    }

    private void muteCall(NetworkPackage np) {

    }

    private void declineCall(NetworkPackage np) {

    }

    private void answerCall(NetworkPackage np) {
        TelecomManager tm = (TelecomManager) BackgroundService.getAppContext()
                .getSystemService(Context.TELECOM_SERVICE);

        if (tm == null) {
            // whether you want to handle this is up to you really
            throw new NullPointerException("TelecomManager == null");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tm.acceptRingingCall();
        }

    }
}
