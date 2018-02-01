package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.buhalo.lazyir.R;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by buhalo on 14.01.18.
 */

public class SettingService {
    SharedPreferences sharedPreferences;
    private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    public String getValue(String key){ // return some standart value if null
        reentrantReadWriteLock.writeLock().lock();
        try {
            if (sharedPreferences == null)
                initialInit();
            return sharedPreferences.getString(key, null);
        }finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public void setValue(String key,String value){
        reentrantReadWriteLock.writeLock().lock();
        try {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(key, value);
            edit.apply();
        }finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public void setValue(String key,Integer value){
        reentrantReadWriteLock.writeLock().lock();
        try {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt(key, value);
            edit.apply();
        }finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public void setValue(String key,Long value){
        reentrantReadWriteLock.writeLock().lock();
        try {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putLong(key, value);
            edit.apply();
        }finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public void setValue(String key,Float value){
        reentrantReadWriteLock.writeLock().lock();
        try {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putFloat(key, value);
            edit.apply();
        }finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public void removeValue(String key){
        reentrantReadWriteLock.writeLock().lock();
        try {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.remove(key);
            edit.apply();
        }finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    private void initialInit(){
        sharedPreferences = BackgroundService.getAppContext().getSharedPreferences(SettingService.class.getSimpleName(), Context.MODE_PRIVATE);
        if(!sharedPreferences.contains("callFrequency"))
            setValue("callFrequency","180");
        if(!sharedPreferences.contains("smsFrequency"))
            setValue("smsFrequency","180");
        if(!sharedPreferences.contains("Sftp-port"))
            setValue("Sftp-port","9000");
        if(!sharedPreferences.contains("TCP-port"))
            setValue("TCP-port","5667");
    }
}
