package com.example.buhalo.lazyir.service.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.example.buhalo.lazyir.modules.notification.CallSmsUtils;
import com.example.buhalo.lazyir.modules.notification.sms.Sms;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;

import static com.example.buhalo.lazyir.service.BackgroundUtil.checkWifiOnAndConnected;

public class SmsListener extends BroadcastReceiver {

    @Inject @Getter @Setter
    CallSmsUtils utils;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this,context);
        String action = intent.getAction();
        if(!checkWifiOnAndConnected(context) || action == null) {
            return;
        }
        if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
            BackgroundUtil.addCommand(BackgroundServiceCmds.CACHE_CONNECT,context);
            final Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            final Object[] pdus = (Object[]) bundle.get("pdus");
            if(pdus == null) {
                return;
            }
            for (Object pdu :  pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                stringBuilder.append(smsMessage.getMessageBody());
            }
            Sms resultMessage =  extractSms( SmsMessage.createFromPdu((byte[])pdus[0]),context);
            resultMessage.setText(stringBuilder.toString());
            smsReceive(resultMessage);
        }
    }

    private Sms extractSms(SmsMessage smsMessage,Context context){
        String messageBody = smsMessage.getMessageBody();
        String name = utils.getName( smsMessage.getOriginatingAddress(),context.getApplicationContext());
        String icon = utils.getContactImage(context,smsMessage.getOriginatingAddress());
        String phoneNumber = utils.getPhoneNumber(name);
        Sms sms = new Sms(name, phoneNumber, messageBody);
        sms.setIcon(icon);
        return sms;
    }

    private void smsReceive(Sms sms) {
        if(!BackgroundUtil.hasActualConnection()){
            BackgroundUtil.getTimerExecutor().schedule(()-> EventBus.getDefault().post(sms),700, TimeUnit.MILLISECONDS);
        }else {
            EventBus.getDefault().post(sms);
        }
    }

}
