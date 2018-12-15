package com.example.buhalo.lazyir.modules;

import android.util.Log;

import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.di.ModuleComponent;
import com.example.buhalo.lazyir.modules.battery.Battery;
import com.example.buhalo.lazyir.modules.battery.BatteryDto;
import com.example.buhalo.lazyir.modules.clipboard.ClipBoardDto;
import com.example.buhalo.lazyir.modules.dbus.MprisDto;
import com.example.buhalo.lazyir.modules.memory.Memory;
import com.example.buhalo.lazyir.modules.memory.MemoryDto;
import com.example.buhalo.lazyir.modules.notification.call.CallModule;
import com.example.buhalo.lazyir.modules.notification.call.CallModuleDto;
import com.example.buhalo.lazyir.modules.notification.messengers.MessengersDto;
import com.example.buhalo.lazyir.modules.notification.notifications.ShowNotificationDto;
import com.example.buhalo.lazyir.modules.notification.reminder.ReminderDto;
import com.example.buhalo.lazyir.modules.notification.sms.SmsModuleDto;
import com.example.buhalo.lazyir.modules.ping.Ping;
import com.example.buhalo.lazyir.modules.notification.reminder.Reminder;
import com.example.buhalo.lazyir.modules.ping.PingDto;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommand;
import com.example.buhalo.lazyir.modules.clipboard.ClipBoard;
import com.example.buhalo.lazyir.modules.dbus.Mpris;
import com.example.buhalo.lazyir.modules.notification.messengers.Messengers;
import com.example.buhalo.lazyir.modules.notification.notifications.ShowNotification;
import com.example.buhalo.lazyir.modules.notification.sms.SmsModule;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommandDto;
import com.example.buhalo.lazyir.modules.share.ShareModule;
import com.example.buhalo.lazyir.modules.share.ShareModuleDto;
import com.example.buhalo.lazyir.modules.touch.KeyboardControl;
import com.example.buhalo.lazyir.modules.touch.KeyboardDto;
import com.example.buhalo.lazyir.modules.touch.TouchControl;
import com.example.buhalo.lazyir.modules.touch.TouchControlDto;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.utils.entity.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;


import lombok.Getter;
import lombok.Synchronized;


public class ModuleFactory {
    private static final String TAG = "ModuleFactory";

    @Getter
    private static HashMap<String, Pair<Class,Class>> registeredModules = new HashMap<>();
    private ModuleComponent moduleComponent;
    private Method[] methods;

    public ModuleFactory() {
        registerModulesInit();
    }

    @Synchronized
    private Module instantiateModule(Device dv, Class registeredModule) {
        try {
            if(moduleComponent == null){
                moduleComponent = BackgroundUtil.getAppComponent().getModuleComponent();
            }
            if(registeredModules.isEmpty()){
                registerModulesInit();
            }
            Method method = getMethod(registeredModule);
            if(method == null) {
                Log.e(TAG,"NullPointerException Such method doesn't exist  " + registeredModule.getSimpleName());
                throw new NullPointerException("Such method doesn't exist  " + registeredModule.getSimpleName());
            }
            method.setAccessible(true);
            Module module =(Module) method.invoke(moduleComponent);
            module.setDevice(dv);
            return module;
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG,"error in instantiateModule - " + dv.getId() + " " + registeredModule,e);
        }
        return null;
    }


    private Method getMethod(Class registeredModule) {
        if (methods == null) {
            methods = moduleComponent.getClass().getDeclaredMethods();
        }
        Method method = null;
        for (Method mt : methods) {
            if(mt.getName().equals("provide"+registeredModule.getSimpleName())){
                method = mt;
                break;
            }
        }
        return method;
    }

    private void registerModulesInit() {
        if(registeredModules.isEmpty()) {
            registeredModules.put("SendCommand",new Pair<>(SendCommand.class, SendCommandDto.class));
            registeredModules.put("ShareModule",new Pair<>(ShareModule.class, ShareModuleDto.class));
            registeredModules.put("ShowNotification",new Pair<>(ShowNotification.class, ShowNotificationDto.class));
            registeredModules.put("SmsModule",new Pair<>(SmsModule.class, SmsModuleDto.class));
            registeredModules.put("Battery",new Pair<>(Battery.class, BatteryDto.class));
            registeredModules.put("Mpris",new Pair<>(Mpris.class, MprisDto.class));
            registeredModules.put("ClipBoard",new Pair<>(ClipBoard.class, ClipBoardDto.class));
            registeredModules.put("Messengers",new Pair<>(Messengers.class, MessengersDto.class));
            registeredModules.put("TouchControl",new Pair<>(TouchControl.class, TouchControlDto.class));
            registeredModules.put("CallModule",new Pair<>(CallModule.class, CallModuleDto.class));
            registeredModules.put("Reminder",new Pair<>(Reminder.class, ReminderDto.class));
            registeredModules.put("Memory",new Pair<>(Memory.class, MemoryDto.class));
            registeredModules.put("Ping",new Pair<>(Ping.class, PingDto.class));
            registeredModules.put("KeyboardControl",new Pair<>(KeyboardControl.class, KeyboardDto.class));
        }
    }


    public Module instantiateModuleByName(Device dv,String name) {
        Pair<Class, Class> entry = registeredModules.get(name);
        if(entry == null){
            return null;
        }
        Class moduleClass = entry.getLeft();
        return instantiateModule(dv, moduleClass);
    }

    public Class getModuleDto(String type){
        Pair<Class, Class> pair = registeredModules.get(type);
        if(pair == null){
            return null;
        }
        return pair.getRight();
    }

}
