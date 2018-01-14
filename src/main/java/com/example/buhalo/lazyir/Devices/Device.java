package com.example.buhalo.lazyir.Devices;

import android.content.Context;

import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.service.network.tcp.ConnectionThread;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by buhalo on 19.02.17.
 */

public class Device {
    private static ConcurrentHashMap<String,Device> connectedDevices = new ConcurrentHashMap<>();
    private Socket socket;
    private String id;
    private String name;

    // connection thread for this device, device will communicate over this thread
    private ConnectionThread thread;
    //add type, to future purposes
    private String deviceType;

    private InetAddress ip;
    private volatile boolean paired;
    private volatile boolean listening;
    private volatile boolean pinging;
    private volatile boolean answer;
    private ConcurrentHashMap<String,Module> enabledModules = new ConcurrentHashMap<>();
    private List<ModuleSetting> enabledModulesConfig;


    public Device(Socket socket, String id, String name, InetAddress ip, ConnectionThread thread,List<ModuleSetting> enabledModules,Context context) {
        this.socket = socket;
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.paired = false;
        this.thread = thread;
        this.listening = false;
        this.pinging = false;
        this.answer = false;
        this.deviceType = "pc";   // by default device type is PC;
        this.enabledModulesConfig = enabledModules;
        if(enabledModules != null)
        for (ModuleSetting registeredModule : enabledModules) {
            if(registeredModule.isEnabled()){
                System.out.println(registeredModule.getName());
                enableModule(registeredModule.getName());
            } }
    }

    public Device(Socket socket, String id, String name, InetAddress ip, String deviceType, ConnectionThread thread,List<ModuleSetting> enabledModules,Context context) {
       this(socket,id,name,ip,thread,enabledModules,context);
       this.deviceType = deviceType;
    }

    public static Map<String, Device> getConnectedDevices() {
        return connectedDevices;
    }

    public static void setConnectedDevices(ConcurrentHashMap<String, Device> connectedDevices) {
        connectedDevices = connectedDevices;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }


    public void sendMessage(String message)
    {
        if(thread != null && thread.isConnected())
        thread.printToOut(message);
    }

    public boolean isConnected()
    {
        return thread != null && thread.isConnected();
    }

    public void closeConnection()
    {
        thread.closeConnection();
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public boolean isPinging() {
        return pinging;
    }

    public void setPinging(boolean pinging) {
        this.pinging = pinging;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }

    public void disableModules()
    {
        enabledModules.clear();
    }


    public ConcurrentHashMap<String,Module> getEnabledModules()
    {
        return enabledModules;
    }

    public Context getContext() {
        return thread.getContext();
    }

    public void enableModule(String moduleName) {
        Module module = ModuleFactory.instantiateModuleByName(this, moduleName);
        if(module != null)
            enabledModules.put(moduleName, module);
    }

    public void disableModule(String moduleName) {
        enabledModules.get(moduleName).endWork();
        enabledModules.remove(moduleName);
    }


    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void refreshEnabledModules(List<ModuleSetting> moduleSettingList) {
        setEnabledModulesConfig(moduleSettingList);
        for (ModuleSetting moduleSetting : moduleSettingList) {
            String name = moduleSetting.getName();
            if(enabledModules.containsKey(name) && !moduleSetting.isEnabled()){ // if contain in enabledModules, but in income list is disabled
                disableModule(name);
            }else if(!enabledModules.containsKey(name) && moduleSetting.isEnabled()){ // opposite case, don't contain in enabledModules, but in list is enabled
                enableModule(name);                                             // instantiate module, and put to enabledModules!
            }
        }

    }

    public List<ModuleSetting> getEnabledModulesConfig() {
        return enabledModulesConfig;
    }

    public void setEnabledModulesConfig(List<ModuleSetting> enabledModulesConfig) {
        this.enabledModulesConfig = enabledModulesConfig;
    }
}
