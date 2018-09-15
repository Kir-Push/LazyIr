package com.example.buhalo.lazyir.modules.battery;

import android.content.Context;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

public class Battery extends Module {

    @Inject
    public Battery(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void execute(NetworkPackage np) {
        // you don't have any commands from pc
    }

    @Override
    public void endWork() {
        context = null;
        device = null;
        EventBus.getDefault().unregister(this);
    }



    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onBatteryBroadcastReceived(BatteryDto event) {
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,event));
    }

}
