package com.example.buhalo.lazyir.modules.notificationModule.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundService;

import static com.example.buhalo.lazyir.modules.notificationModule.CallSmsUtils.getContactImage;
import static com.example.buhalo.lazyir.modules.notificationModule.CallSmsUtils.getName;
import static com.example.buhalo.lazyir.modules.notificationModule.CallSmsUtils.getPhoneNumber;

/**
 * Created by buhalo on 30.11.17.
 */

// todo create mms handle, for receiving mms and pictures and other multimedia.
public class SmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action == null)
            return;
        if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
            final Bundle bundle = intent.getExtras();
            if (bundle == null) return;
            StringBuilder stringBuilder = new StringBuilder();
            final Object[] pdus = (Object[]) bundle.get("pdus");
            if(pdus == null)
                return;
            for (Object pdu :  pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                stringBuilder.append(smsMessage.getMessageBody());
            }
            Sms resultMessage =  extractSms( SmsMessage.createFromPdu((byte[])pdus[0]),context);
            resultMessage.setText(stringBuilder.toString());
            smsReceive(resultMessage, context);
        }
    }

    private Sms extractSms(SmsMessage smsMessage,Context context){
        String messageBody = smsMessage.getMessageBody();
        String name = getName( smsMessage.getOriginatingAddress(),context.getApplicationContext());
        String icon = getContactImage(context,smsMessage.getOriginatingAddress());
        String phoneNumber = getPhoneNumber(name,context);
        return new Sms(name,phoneNumber,messageBody,icon,null);
    }

    private void smsReceive(Sms sms, Context context) {
        if(sms == null)
            return;
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SmsModule.SMS_TYPE,SmsModule.RECEIVE);
        np.setObject(NetworkPackage.N_OBJECT,sms);
        System.out.println("sending sms: " + sms);
        BackgroundService.sendToAllDevices(np.getMessage());
    }

}
