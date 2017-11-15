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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by buhalo on 19.02.17.
 */

public class Device {
    public static ConcurrentHashMap<String,Device> connectedDevices = new ConcurrentHashMap<>();
    private Context context;
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


    public Device(Socket socket, String id, String name, InetAddress ip, ConnectionThread thread,Context context) {
        this.socket = socket;
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.paired = false;
        this.thread = thread;
        this.listening = false;
        this.pinging = false;
        this.answer = false;
        this.context = context;
        this.deviceType = "pc";   // by default device type is PC;

        // todo do same in server!
        enableModules();
    }

    public Device(Socket socket, String id, String name, InetAddress ip, String deviceType, ConnectionThread thread,Context context) {
       this(socket,id,name,ip,thread,context);
       this.deviceType = deviceType;
    }

    public static Map<String, Device> getConnectedDevices() {
        return connectedDevices;
    }

    public static void setConnectedDevices(ConcurrentHashMap<String, Device> connectedDevices) {
        Device.connectedDevices = connectedDevices;
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


    public void printToOut(String message)
    {
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
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void enableModule(String moduleName, Module module) {
        enabledModules.put(moduleName,module);
    }

    public void disableModule(String moduleName) {
        enabledModules.remove(moduleName);
    }

    public void enableModules() {enabledModules = ModuleFactory.getEnabledModules(this,context);}

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
