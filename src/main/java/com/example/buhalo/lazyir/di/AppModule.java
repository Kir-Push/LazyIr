package com.example.buhalo.lazyir.di;


import com.example.buhalo.lazyir.di.scope.Service;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.listeners.NotificationListener;
import com.example.buhalo.lazyir.service.receivers.CallReceiver;
import com.example.buhalo.lazyir.service.receivers.NotifActionReceiver;
import com.example.buhalo.lazyir.service.receivers.SmsListener;



import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Module(includes = {AndroidSupportInjectionModule.class})
public abstract class AppModule {

    @Service
    @ContributesAndroidInjector()
    abstract BackgroundService backgroundServiceInjector();

    @Service
    @ContributesAndroidInjector()
    abstract NotificationListener notificationListenerInjector();

    @Service
    @ContributesAndroidInjector()
    abstract SmsListener smsListenerInjector();

    @Service
    @ContributesAndroidInjector()
    abstract CallReceiver callReceiverInjector();

    @Service
    @ContributesAndroidInjector()
    abstract NotifActionReceiver notifActionReceiverInjector();

}
