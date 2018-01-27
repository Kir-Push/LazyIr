package com.example.buhalo.lazyir.modules.notificationModule.sms;

import android.telephony.SmsManager;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import java.util.ArrayList;

import static com.example.buhalo.lazyir.modules.notificationModule.CallSmsUtils.getPhoneNumber;

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
            if(message != null)
            send_sms(message);
        }

    }

    @Override
    public void endWork() {

    }
 // todo create send_mms for sending pictures
    private void send_sms(Sms sms) {
        String error = null;
        System.out.println(sms);
        try {
            String number = getPhoneNumber(sms.getName(),context.getApplicationContext());
            if(number.equals("Unsaved")) {
                number = sms.getName();
            }else{
                number = number.replaceAll("-","");
            }
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> messages = smsManager.divideMessage(sms.getText());
            smsManager.sendMultipartTextMessage(number,null,messages,null,null);
          //  smsManager.sendTextMessage(number, null, text, null, null);
        } catch (Exception e) {
            Log.e("SmsModule","error in send_sms",e);
            sendResponse("Message Not Sended");
            return;
        }
        sendResponse("Message Sended");
    }



    private void sendResponse(String response)
    {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SMS_TYPE,RESPONSE);
        np.setValue("response",response);
        sendMsg(np.getMessage());
    }


}
