package com.example.buhalo.lazyir.modules;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.sendIr.SendIr;

/**
 * Created by buhalo on 05.03.17.
 */

public class ModuleExecutor {


    public static void executePackage(final NetworkPackage np)
    {
        String type = np.getType();
        final Module module = Device.getConnectedDevices().get(np.getDvId()).getEnabledModules().get(type);
        if(module == null)
            return;

                module.execute(np);
    }

    public static void executePackageIrOffline(final NetworkPackage np, Context context)
    {
        if(Device.getConnectedDevices().get(np.getDvId()) == null) // if null maybe it's only ir command? trying to do
        {
            Log.d("ModuleExecutor","Start ir executore offline");
            SendIr ir = new SendIr();
            ir.setContext(context);
            ir.execute(np);
        }
    }

}
