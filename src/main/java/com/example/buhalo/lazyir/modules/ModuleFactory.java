package com.example.buhalo.lazyir.modules;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.modules.notificationModule.call.CallModule;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommand;
import com.example.buhalo.lazyir.modules.sendIr.SendIr;
import com.example.buhalo.lazyir.modules.clipBoard.ClipBoard;
import com.example.buhalo.lazyir.modules.dbus.Mpris;
import com.example.buhalo.lazyir.modules.notificationModule.messengers.Messengers;
import com.example.buhalo.lazyir.modules.notificationModule.notifications.ShowNotification;
import com.example.buhalo.lazyir.modules.notificationModule.sms.SmsModule;
import com.example.buhalo.lazyir.modules.shareManager.ShareModule;
import com.example.buhalo.lazyir.modules.synchro.SynchroModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private static Module instantiateModule(Device dv, Class registeredModule) throws IllegalAccessException,InstantiationException
    {
        Module module = null;
        if(registeredModules == null)
        {
            registerModulesInit();
        }
            module = (Module)registeredModule.newInstance();
            module.setDevice(dv);
            module.setContext(dv.getContext());
        return module;
    }

    //method return fully instanced modules, ready to work.
    public static ConcurrentHashMap<String,Module> getEnabledModules(Device dv, Context context)
    {
        lock.lock();
        ConcurrentHashMap<String,Module> resultMap = new ConcurrentHashMap<>();
        try {
            if(dv == null)
                return resultMap;
            // check if Db contain some info about device modules, if no instanciate by default value's(all modules). It all in DBhelper class.
            List<String>  enabledModulesNames = DBHelper.getInstance(context).checkAndSetDefaultIfNoInfo(dv);
            // getting list of enabledModules names from Database;
            if(enabledModulesNames == null || enabledModulesNames.size() == 0)
            enabledModulesNames = DBHelper.getInstance(context).getEnabledModules(dv);
            //instantiate modules. Modules must itself handle multiple instances and so.
            for (String ModuleName : enabledModulesNames) {
                Module module = instantiateModuleByName(dv, ModuleName);
                // check module to null before add to hashMap. If you add null, it may cause some bad things in future.
                if(module != null)
                resultMap.put(ModuleName,module);
            }

        }catch (IllegalAccessException | InstantiationException e) {
            Log.e("ModuleFactory","getEnabledModules",e);
        }finally {
            lock.unlock();
        }
        return resultMap;
    }
    //enable or disable module. Change value on Db and call method
    // if enableOrDisable true, then enable and instanciate module, otherwise disable
    public static void changeModuleStatus(Device dv,String moduleName,Context context,boolean enableOrDisable)
    {
        lock.lock();
        try{
            DBHelper.getInstance(context).changeModuleStatus(dv,moduleName,enableOrDisable);
            if(enableOrDisable)
                dv.enableModule(moduleName,instantiateModuleByName(dv,moduleName));
            else
            dv.disableModule(moduleName);
        }catch (IllegalAccessException | InstantiationException e) {
            Log.e("ModuleFactory","disableModule",e);
        }finally {
            lock.unlock();
        }
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
        registeredModules.add(CallModule.class);
    }

    private static Module instantiateModuleByName(Device dv,String name) throws  IllegalAccessException,InstantiationException
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
    public static List<ModulesWrap> getModulesNamesWithStatus(Device dv,Context context)
    {
        //to
        List<ModulesWrap> result = new ArrayList<>();
        if(dv == null)
            return result;
                    Map<String, Module> enabledModules = getEnabledModules(dv, context);
        List<Class> registeredModules = getRegisteredModules();
        for (Class registeredModule : registeredModules) {
            if(!enabledModules.containsKey(registeredModule.getSimpleName()))
            result.add(new ModulesWrap(registeredModule.getSimpleName(),false, dv.getId()));
        }
        for (String s : enabledModules.keySet()) {
            result.add(new ModulesWrap(s,true, dv.getId()));
        }

        return result;
    }

}
