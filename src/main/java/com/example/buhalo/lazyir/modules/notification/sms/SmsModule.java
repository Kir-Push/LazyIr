package com.example.buhalo.lazyir.modules.notification.sms;

import android.content.Context;
import android.telephony.SmsManager;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.notification.CallSmsUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import static com.example.buhalo.lazyir.modules.notification.sms.SmsModule.api.RECEIVE;
import static com.example.buhalo.lazyir.modules.notification.sms.SmsModule.api.SEND;

public class SmsModule extends Module {
    public enum api{
        SEND,
        RECEIVE,
        RESPONSE
    }

    private CallSmsUtils utils;

    @Inject
    public SmsModule(MessageFactory messageFactory, Context context, CallSmsUtils utils) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
        this.utils = utils;
    }

    @Override
    public void execute(NetworkPackage np) {
        SmsModuleDto dto = (SmsModuleDto) np.getData();
        if(dto.getCommand().equals(SEND.name())){
            sendSms( dto.getSms());
        }

    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
    }

    private void sendSms(Sms sms) {
            String number = utils.getPhoneNumber(sms.getName());
            if(number.equals("Unsaved")) {
                number = sms.getName();
            }else{
                number = number.replaceAll("-","");
            }
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> messages = smsManager.divideMessage(sms.getText());
            smsManager.sendMultipartTextMessage(number,null,messages,null,null);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void sendSmsToServer(Sms sms){
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,new SmsModuleDto(RECEIVE.name(),sms)));
    }


}
