package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.device.ModuleSetting;
import com.example.buhalo.lazyir.di.AppComponent;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.modules.ModulesWrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

public final class BackgroundUtil {

    @Setter @Getter
    private static AppComponent appComponent;

    private static String selectedId = "";

    private BackgroundUtil() {
    }

    public static ConcurrentHashMap<String, Device> getConnectedDevices(){
        return BackgroundService.getConnectedDevices();
    }

    @Synchronized
    public static String getSelectedId(){
        ConcurrentHashMap<String, Device> connectedDevices = BackgroundService.getConnectedDevices();
        if(!connectedDevices.isEmpty() && !connectedDevices.containsKey(selectedId) ){
            selectedId = connectedDevices.keySet().iterator().next();
        }
        return selectedId;
    }

    public static void setSelectedId(String id){
        ConcurrentHashMap<String, Device> connectedDevices = BackgroundService.getConnectedDevices();
        if(!connectedDevices.isEmpty() && connectedDevices.containsKey(id)){
            selectedId = id;
        }
    }

    public static boolean ifLastConnectedDeviceAreYou(String id){
        ConcurrentHashMap<String, Device> connectedDevices = BackgroundService.getConnectedDevices();
        Device device = connectedDevices.get(id);
        int size = connectedDevices.size();
        return ((device != null && device.getId().equals(id) && size == 1) || size == 0);
    }

    public static boolean  hasActualConnection(){
        ConcurrentHashMap<String, Device> connectedDevices = BackgroundService.getConnectedDevices();
        return Stream.of(connectedDevices.values()).filter(Device::isConnected).findFirst().isPresent();
    }

    public static Map<String, ModuleSetting> getMyEnabledModules() {
        return BackgroundService.getMyEnabledModules();
    }

    public static List<ModulesWrap> getModulesWithStatus(){
        List<ModulesWrap> modulesWraps = new ArrayList<>();
        Map<String, ModuleSetting> myEnabledModules = getMyEnabledModules();
        Stream.of( ModuleFactory.getRegisteredModules().keySet()).forEach(moduleName -> modulesWraps.add(new ModulesWrap(moduleName,myEnabledModules.containsKey(moduleName))));
        return modulesWraps;

    }

    public static String getMyId() {
        return android.os.Build.SERIAL;
    }

    public static String getMyName() {
        return android.os.Build.MODEL;
    }

    public static void addCommand(BackgroundServiceCmds cmd, Context context){
        BackgroundService.addCommandToQueue(cmd,context);
    }

    public static void submitTask(Runnable task,Context context){
        BackgroundService.submitNewTask(task,context);
    }

    public static void sendToDevice(String id,String message,Context context){
        BackgroundService.sendToDevice(id,message,context);
    }

    public static void sendToAll(String message,Context context){
        BackgroundService.sendToAllDevices(message,context);
    }

    public static Device getDevice(String id){
        return  BackgroundService.getConnectedDevices().get(id);
    }

    public static void addDeviceToConnected(String id,Device device){
        BackgroundService.getConnectedDevices().put(id,device);
    }

    public static void removeDeviceConnected(String id){
        BackgroundService.getConnectedDevices().remove(id);
    }

    public static boolean checkWifiOnAndConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr != null && wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            return wifiInfo != null && wifiInfo.getNetworkId() != -1;
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public static boolean checkExistingConnection(String dvId) {
        Device device = BackgroundService.getConnectedDevices().get(dvId);
        return device != null && device.isConnected();
    }

    public static ScheduledThreadPoolExecutor getTimerExecutor(){
        return BackgroundService.getTimerService();
    }

    public static void enableModule(String moduleName, DBHelper dbHelper) {
        getTimerExecutor().schedule(()->{
            dbHelper.changeModuleStatus(BackgroundUtil.getMyId(),moduleName,true);
            getMyEnabledModules().put(moduleName,new ModuleSetting(moduleName,true, Collections.emptyList(),true));
            Stream.of(BackgroundService.getConnectedDevices().values()).forEach(dv -> dv.enableModule(moduleName));
        },0, TimeUnit.MILLISECONDS);
    }

    public static void disableModule(String moduleName, DBHelper dbHelper) {
        getTimerExecutor().schedule(()-> {
            dbHelper.changeModuleStatus(BackgroundUtil.getMyId(), moduleName, false);
            getMyEnabledModules().remove(moduleName);
            Stream.of(BackgroundService.getConnectedDevices().values()).forEach(dv -> dv.disableModule(moduleName));
        },0,TimeUnit.MILLISECONDS);
    }
}
