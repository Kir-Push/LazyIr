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


    private static List<Class> registeredModules;

    public static Module instantiateModule(Device dv, Class registeredModule)
    {
        if(registeredModules == null)
        {
            registerModulesInit();
        }
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

    private static void registerModulesInit() {
        registeredModules = new ArrayList<>();
        registeredModules.add(SendCommand.class);
        registeredModules.add(SendIr.class);
        registeredModules.add(ShareModule.class);
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

    public static List<Class> getRegisteredModules() {
        if(registeredModules == null)
        {
            registerModulesInit();
        }
        return registeredModules;
    }

    public static void setRegisteredModules(List<Class> registeredModules) {
        ModuleFactory.registeredModules = registeredModules;
    }

}
