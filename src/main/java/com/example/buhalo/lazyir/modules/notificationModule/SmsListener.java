package com.example.buhalo.lazyir.modules.notificationModule;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
            final Object[] pdus = (Object[]) bundle.get("pdus");
            for (Object pdu : pdus != null ? pdus : new Object[0]) {
                SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                smsReceive(message, context);
            }
        }
    }

    private void smsReceive(SmsMessage smsMessage,Context context) {
        String messageBody = smsMessage.getMessageBody();
        String name = getName( smsMessage.getOriginatingAddress(),context.getApplicationContext());
        String icon = getContactImage(context,smsMessage.getOriginatingAddress());
        String phoneNumber = getPhoneNumber(name,context);
        Sms sms = new Sms(name,phoneNumber,messageBody,icon,null);
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SmsModule.SMS_TYPE,SmsModule.RECEIVE);
        np.setObject(NetworkPackage.N_OBJECT,sms);
        BackgroundService.sendToAllDevices(np.getMessage());
    }

}
