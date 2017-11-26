package com.example.buhalo.lazyir.modules;

import android.content.Context;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 05.03.17.
 */

public abstract class Module {

    protected Device device;

    protected  Context context;

    protected Lock lock = new ReentrantLock();
    protected volatile boolean working = true;

    public   void setDevice(Device dv)
    {
        this.device = dv;
    }

    public  void setContext(Context context)
    {
        this.context = context;
    }

    public abstract void execute(NetworkPackage np);


    public void endWork() {
        lock.lock();
        try {
            working = false;
        }finally {
            lock.unlock();
        }
    }

    public void sendMsg(String msg) {
        BackgroundService.sendToDevice(device.getId(),msg);
    }


    public void sendToAll(String msg) {BackgroundService.sendToAllDevices(msg);}
}
