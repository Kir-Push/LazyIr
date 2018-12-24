package com.example.buhalo.lazyir.modules;

import android.content.Context;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.service.BackgroundUtil;


public abstract class Module {

    protected Device device;
    protected MessageFactory messageFactory;
    protected  Context context;

    public Module(MessageFactory messageFactory, Context context) {
        this.messageFactory = messageFactory;
        this.context = context;
    }

    public void setDevice(Device dv)
    {
        this.device = dv;
    }
    public abstract void execute(NetworkPackage np);
    public abstract void endWork();
    protected void sendMsg(String msg) {
        if(device.isPaired()) {
            BackgroundUtil.sendToDevice(device.getId(), msg, context);
        }}
    protected void sendToAll(String msg) { BackgroundUtil.sendToAll(msg,context);}

}
