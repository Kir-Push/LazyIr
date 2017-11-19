package com.example.buhalo.lazyir.modules;

import android.content.Context;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

/**
 * Created by buhalo on 05.03.17.
 */

public abstract class Module {

    protected Device device;

    protected  Context context;

    public   void setDevice(Device dv)
    {
        this.device = dv;
    }

    public  void setContext(Context context)
    {
        this.context = context;
    }

    public abstract void execute(NetworkPackage np);

    public void sendMsg(String msg) {
        BackgroundService.sendToDevice(device.getId(),msg);
    }


    public void sendToAll(String msg) {BackgroundService.sendToAllDevices(msg);}
    public abstract void endWork();
}
