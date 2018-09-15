package com.example.buhalo.lazyir.modules.touch;



import android.content.Context;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


public class TouchControl extends Module {
    public enum api{
        MOVE,
        CLICK,
        DCLICK,
        RCLICK,
        MOUSEUP,
        MOUSEDOWN,
        MOUSECLICK,
        LONGCLICK,
        LONGRELEASE
    }

    @Inject
    public TouchControl(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
    }

    public void execute(NetworkPackage np) {
        //server doesn't send anything
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void sendToServer(TouchControlDto dto){
        if(dto.getId().equals(device.getId())) {
            String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, dto);
            sendMsg(message);
        }
    }
}
