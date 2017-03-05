package com.example.buhalo.lazyir.modules;

import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.modules.SendCommand.SendCommand;
import com.example.buhalo.lazyir.modules.SendIr.SendIr;
import com.example.buhalo.lazyir.modules.shareManager.ShareModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

public class ModuleFactory {

    public static List<Class> registeredModules = new ArrayList<>();
    {
        registeredModules.add(SendCommand.class);
       registeredModules.add(SendIr.class);
        registeredModules.add(ShareModule.class);
    }

    public static Module instantiateModule(Device dv, Class registeredModule)
    {
        Module module = null;
        try {
            module = (Module)registeredModule.newInstance();
            module.setDevice(dv);
            module.setContext(dv.getContext());
        } catch (IllegalAccessException | InstantiationException e) {
            Log.e("ModuleFactory",e.toString());
        }
        return module;
    }

    public static Module instantiateModuleByName(Device dv,String name)
    {
        for (Class registeredModule : registeredModules) {
            if(registeredModule.getSimpleName().equals(name))
            {
                return instantiateModule(dv,registeredModule);
            }
        }
        return null;

    }

}
