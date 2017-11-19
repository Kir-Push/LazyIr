package com.example.buhalo.lazyir.modules.notificationModule;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.util.ArrayList;

/**
 * Created by buhalo on 26.03.17.
 */

public class SmsModule extends Module {

    public static final String SMS_TYPE = "SmsModule";
    public static final String SEND = "send";
    public static final String RESPONSE = "response";
    public static final String RECEIVE = "receive";

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(SEND))
        {
            Sms message = np.getObject(NetworkPackage.N_OBJECT,Sms.class);
            if(message.getName() != null)
            send_sms(message.getName(),message.getText(),np.getId());
        }

    }

    @Override
    public void endWork() {

    }

    private void send_sms(String name,String text,String dvId) {
        String error = null;
        try {
            String number = getPhoneNumber(name,context.getApplicationContext());
            if(number.equals("Unsaved"))
            {
                number = name;
            }
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> messages = smsManager.divideMessage(text);
            smsManager.sendMultipartTextMessage(number,null,messages,null,null);
          //  smsManager.sendTextMessage(number, null, text, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            error = ex.getMessage();
            sendResponse(error,dvId);
            return;
        }
        sendResponse("Message Sended",dvId);
    }

    public String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.Contacts.DISPLAY_NAME+" like '" + name +"'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        if(ret==null)
            ret = "Unsaved";
        return ret;
    }


    public static String getName(String number, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER+" like " + number +"";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor c = context.getContentResolver().query(contactUri,
                projection, null, null, null);
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        if(ret==null)
            ret = number;
        return ret;
    }

    private void sendResponse(String response,String dvId)
    {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SMS_TYPE,RESPONSE);
        np.setValue("response",response);
        BackgroundService.sendToDevice(dvId,np.getMessage());
    }


}
