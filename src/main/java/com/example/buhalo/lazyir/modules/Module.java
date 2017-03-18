package com.example.buhalo.lazyir.modules;

import android.content.Context;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;

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


}
