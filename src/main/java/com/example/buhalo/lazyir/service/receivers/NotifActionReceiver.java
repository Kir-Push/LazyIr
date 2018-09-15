package com.example.buhalo.lazyir.service.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.buhalo.lazyir.modules.ping.Ping;
import com.example.buhalo.lazyir.modules.ping.PingDto;
import com.example.buhalo.lazyir.service.network.tcp.PairService;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.buhalo.lazyir.service.BackgroundUtil.checkWifiOnAndConnected;


public class NotifActionReceiver extends BroadcastReceiver {
    
    @Inject @Getter @Setter
    PairService pairService;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this,context);
        if(!"lazyIr-cmd".equals(intent.getAction())){
            return;
        }
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Bundle extras = intent.getExtras();
        if(extras == null){
            return;
        }
        String action = extras.getString("action");
        if("stopAlarm".equals(action)){
            if(mNotifyMgr != null){
                mNotifyMgr.cancel(extras.getInt("id"));
            }
            EventBus.getDefault().post(new PingDto(Ping.api.STOP.name()));
        }
        else if(checkWifiOnAndConnected(context) && "pairAnswer".equals(extras.get("command"))) {
            pairService.pairRequestAnswerFromGui( intent.getStringExtra("user-id"),"Yes".equals(action),intent.getStringExtra("value"),context);
            if(mNotifyMgr != null){
                mNotifyMgr.cancel(extras.getInt("id"));
            }
        }
    }
}
