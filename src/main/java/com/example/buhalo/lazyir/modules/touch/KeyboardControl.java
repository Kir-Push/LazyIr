package com.example.buhalo.lazyir.modules.touch;

import android.content.Context;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

public class KeyboardControl extends Module {
    public enum api{
        PRESS,
        KEYS_UP,
        COMBO,
        SPECIAL_KEYS,
        CHANGE_LANG
    }

    @Inject
    public KeyboardControl(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void execute(NetworkPackage np) {
//don't need
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void sendToServer(KeyboardDto dto){
        if(dto.getId().equals(device.getId())) {
            String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, dto);
            sendMsg(message);
        }
    }
}
