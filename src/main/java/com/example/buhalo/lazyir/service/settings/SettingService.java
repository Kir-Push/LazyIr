package com.example.buhalo.lazyir.service.settings;

import android.content.Context;
import android.content.SharedPreferences;
import javax.inject.Inject;

import lombok.Synchronized;

public class SettingService {

    private Context context;
    private  SharedPreferences sharedPreferences;

    @Inject
    public SettingService(Context context) {
        this.context = context;
    }

    @Synchronized
    public String getValue(String key){
        if (sharedPreferences == null) {
            initialInit();
        }
        return sharedPreferences.getString(key, null);
    }

    @Synchronized
    public void setValue(String key,String value){
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        edit.apply();
    }

    @Synchronized
    public void setValue(String key,Integer value){
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    @Synchronized
    public void setValue(String key,Long value){
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    @Synchronized
    public void removeValue(String key){
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(key);
        edit.apply();
    }

    private void initialInit(){
        sharedPreferences = context.getSharedPreferences(SettingService.class.getSimpleName(), Context.MODE_PRIVATE);
        if(!sharedPreferences.contains("callFrequency")) {
            setValue("callFrequency", "180");
        }
        if(!sharedPreferences.contains("smsFrequency")) {
            setValue("smsFrequency", "180");
        }
        if(!sharedPreferences.contains("Sftp-port")) {
            setValue("Sftp-port", "9000");
        }
        if(!sharedPreferences.contains("TCP-port")) {
            setValue("TCP-port", "5667");
        }
    }
}
