package com.example.buhalo.lazyir.modules;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.battery.Battery;
import com.example.buhalo.lazyir.modules.memory.Memory;
import com.example.buhalo.lazyir.modules.notificationModule.call.CallModule;
import com.example.buhalo.lazyir.modules.ping.Ping;
import com.example.buhalo.lazyir.modules.reminder.Reminder;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommand;
import com.example.buhalo.lazyir.modules.sendIr.IrModule;
import com.example.buhalo.lazyir.modules.clipBoard.ClipBoard;
import com.example.buhalo.lazyir.modules.dbus.Mpris;
import com.example.buhalo.lazyir.modules.notificationModule.messengers.Messengers;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.ShowNotification;
import com.example.buhalo.lazyir.modules.notificationModule.sms.SmsModule;
import com.example.buhalo.lazyir.modules.shareManager.ShareModule;
import com.example.buhalo.lazyir.modules.synchro.SynchroModule;
import com.example.buhalo.lazyir.modules.touch.TouchControl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 05.03.17.
 */
// util class for instantiating and controlling of modules. Enabling,Disabling,Getting list of enabled.
public class ModuleFactory {


    private static Lock lock = new ReentrantLock();
    // container of all refistedModulesclasses
    private static List<Class> registeredModules;
    private static boolean myEnabledModulesUpdate;
    private static HashSet<String> myEnabledModules;

    private static Module instantiateModule(Device dv, Class registeredModule)
    {
        lock.lock();
        try {
            if (registeredModules == null) {
                registerModulesInit();
            }
            Module module = null;
            try {
                module = (Module) registeredModule.newInstance();
                module.setDevice(dv);
                module.setContext(dv.getContext());
            } catch (IllegalAccessException | InstantiationException e) {
                Log.e("ModuleFactory", e.toString());

            }
            return module;
        }finally {
            lock.unlock();
        }
    }

    //method return fully instanced modules, ready to work.
    public static  HashSet<String> getMyEnabledModules(Context context)
    {
        lock.lock();
        if(!myEnabledModulesUpdate && myEnabledModules != null) {
            return myEnabledModules;
        }
        String myId = NetworkPackage.getMyId();
        HashSet<String> result;
        try {
            // check if Db contain some info about device modules, if no instanciate by default value's(all modules). It all in DBhelper class.
            List<String>  enabledModulesNames = DBHelper.getInstance(context).checkAndSetDefaultIfNoInfo(myId);

            // getting list of enabledModules names from Database;
            if(enabledModulesNames == null || enabledModulesNames.size() == 0)
            enabledModulesNames = DBHelper.getInstance(context).getEnabledModules(myId);
            result = new HashSet<>(enabledModulesNames);
            myEnabledModulesUpdate = false;
            myEnabledModules = result;
        }finally {
            lock.unlock();
        }
        return result;
    }
    //enable or disable module. Change value on Db and call method
    // if enableOrDisable true, then enable and instanciate module, otherwise disable
    public static void changeModuleStatus(String moduleName,Context context,boolean enableOrDisable)
    {
        lock.lock();
        try{
            DBHelper.getInstance(context).changeModuleStatus(NetworkPackage.getMyId(),moduleName,enableOrDisable);
            myEnabledModulesUpdate = true;
        }finally {
            lock.unlock();
        }
    }


    private static void registerModulesInit() {
        registeredModules = new ArrayList<>();
        registeredModules.add(SendCommand.class);
        registeredModules.add(IrModule.class);
        registeredModules.add(ShareModule.class);
        registeredModules.add(SmsModule.class);
        registeredModules.add(Mpris.class);
        registeredModules.add(ClipBoard.class);
        registeredModules.add(Messengers.class);
        registeredModules.add(ShowNotification.class);
        registeredModules.add(SynchroModule.class);
        registeredModules.add(CallModule.class);
        registeredModules.add(Battery.class);
        registeredModules.add(TouchControl.class);
        registeredModules.add(Reminder.class);
        registeredModules.add(Memory.class);
        registeredModules.add(Ping.class);
    }

    public static Module instantiateModuleByName(Device dv,String name)
    {
        if(registeredModules == null)
        {
            registerModulesInit();
        }
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

    // method getting all registered modules, get all enabled modules for device, after that iterate over all modules and check if enabledhashMap contain it,
    // if no - add to resultList with false status. After that iterate over enabledModules and add all to resultList.
    // use Module wrapper which is simple class with string name,dv id - (needed in internal logic) and bool status (enabled:disabled);
    // alghorithm not efficient, but number of modules very small - around 10, maximum 15 items,
    // it's not very bad for that. alghorithm has O(2n) difficulty.
    public static List<ModulesWrap> getModulesNamesWithStatus(Context context)
    {
        //to
        List<ModulesWrap> result = new ArrayList<>();
        HashSet<String> enabledModules = getMyEnabledModules(context);
        List<Class> registeredModules = getRegisteredModules();
        for (Class registeredModule : registeredModules) {
            if(!enabledModules.contains(registeredModule.getSimpleName()))
            result.add(new ModulesWrap(registeredModule.getSimpleName(),false, NetworkPackage.getMyId()));
        }
        for (String s : enabledModules) {
            result.add(new ModulesWrap(s,true,NetworkPackage.getMyId()));
        }

        return result;
    }

}
