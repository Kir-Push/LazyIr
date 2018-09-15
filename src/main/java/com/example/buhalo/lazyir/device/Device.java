package com.example.buhalo.lazyir.device;

import android.content.Context;

import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.service.network.tcp.ConnectionThread;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.Synchronized;

@Data
public class Device {
    private ConnectionThread thread;
    private String id;
    private String name;
    private InetAddress ip;
    private String deviceType;
    private boolean paired;
    private boolean listening;
    private boolean pinging;
    private boolean answer;
    private ConcurrentHashMap<String, Module> enabledModules = new ConcurrentHashMap<>();
    private List<ModuleSetting> enabledModulesConfig;
    private ModuleFactory moduleFactory;


    public Device(String id, String name, InetAddress ip, ConnectionThread runnableThread, List<ModuleSetting> enabledModules,ModuleFactory moduleFactory) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.thread = runnableThread;
        this.paired = false;
        this.listening = true;
        this.pinging = false;
        this.answer = false;
        this.deviceType = "phone";
        this.enabledModulesConfig = enabledModules;
        this.moduleFactory = moduleFactory;
    }

    public Context getContext(){
        return thread.getContext();
    }

    @Synchronized
    public void enableModules(){
        Map<String, ModuleSetting> myEnabledModules = BackgroundUtil.getMyEnabledModules();
        Stream.of(enabledModulesConfig).filter(ModuleSetting::isEnabled).filter(module -> myEnabledModules.containsKey(module.getName())).forEach(module -> enableModule(module.getName()));
    }

    public boolean isConnected() {
        return thread != null && thread.isConnected();
    }

    @Synchronized
    public void closeConnection() {
        thread.setConnectionRun(false);
        thread.clearResources();
    }

    @Synchronized
    public void enableModule(String name) {
        if(!enabledModules.containsKey(name)) {
            Module module = moduleFactory.instantiateModuleByName(this, name);
            if (module != null) {
                enabledModules.put(name, module);
            }
        }
    }

    public void sendMessage(String msg) {
        if(isConnected()) {
            thread.printToOut(msg);
        }
    }


    @Synchronized
    public void disableModule(String name){
        Module module = enabledModules.get(name);
        if (module != null) {
            enabledModules.remove(name);
            module.endWork();
        }
    }
    /*
    when use changes it's enabled modules on phone, it send cmd to other device update enabled modules
    here iterate over hashMap of enabledModulesConfig, check if it correspond to income list
    if disable  - end module, and remove from list
    if enable(didn't contain in hashMap) - instantiate module
    * */
    @Synchronized
    public void refreshEnabledModules(List<ModuleSetting> moduleSettingList) {
        if (!isConnected()) {
            return;
        }
        setEnabledModulesConfig(moduleSettingList);
        Stream.of(moduleSettingList).forEach(module -> {
            String moduleName = module.getName();
            if (!module.isEnabled()) {
                disableModule(moduleName);
            } else {
                enableModule(moduleName);
            }
        });
    }
}
