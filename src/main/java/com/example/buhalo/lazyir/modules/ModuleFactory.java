package com.example.buhalo.lazyir.modules;

import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommand;
import com.example.buhalo.lazyir.modules.sendIr.SendIr;
import com.example.buhalo.lazyir.modules.clipBoard.ClipBoard;
import com.example.buhalo.lazyir.modules.dbus.Mpris;
import com.example.buhalo.lazyir.modules.notificationModule.Messengers;
import com.example.buhalo.lazyir.modules.notificationModule.ShowNotification;
import com.example.buhalo.lazyir.modules.notificationModule.SmsModule;
import com.example.buhalo.lazyir.modules.shareManager.ShareModule;
import com.example.buhalo.lazyir.modules.synchro.SynchroModule;

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
        registeredModules.add(SmsModule.class);
        registeredModules.add(Mpris.class);
        registeredModules.add(ClipBoard.class);
        registeredModules.add(Messengers.class);
        registeredModules.add(ShowNotification.class);
        registeredModules.add(SynchroModule.class);
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
