package com.example.buhalo.lazyir;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Service;
import android.content.BroadcastReceiver;

import com.example.buhalo.lazyir.di.ActivityComponent;
import com.example.buhalo.lazyir.di.AppComponent;
import com.example.buhalo.lazyir.di.DaggerAppComponent;
import com.example.buhalo.lazyir.di.ServiceComponent;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import javax.inject.Inject;

import dagger.android.AndroidInjector;;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.HasServiceInjector;
import lombok.Getter;

import static com.example.buhalo.lazyir.service.BackgroundUtil.checkWifiOnAndConnected;

public class AppBoot extends Application implements HasActivityInjector, HasServiceInjector,HasBroadcastReceiverInjector,HasFragmentInjector{

    @Inject
    public DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject
    public DispatchingAndroidInjector<Service> dispatchingServiceInjector;
    @Inject
    public DispatchingAndroidInjector<BroadcastReceiver> dispatchingBroadcastInector;
    @Inject
    public DispatchingAndroidInjector<Fragment> dispatchingFragmentInjector;

    @Getter
    private ActivityComponent activityComponent;
    @Getter
    private ServiceComponent serviceComponent;
    @Getter
    private AppComponent appComponent;


    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder().application(this).build();
        serviceComponent = appComponent.getServiceComponent();
        activityComponent = serviceComponent.getActivityComponent();
        activityComponent.inject(this);
        BackgroundUtil.setAppComponent(appComponent);
        BackgroundUtil.addCommand(BackgroundServiceCmds.REGISTER_BROADCASTS,this);
       if(checkWifiOnAndConnected(this)) {
           BackgroundUtil.addCommand(BackgroundServiceCmds.START_TASKS,this);
       }

    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public DispatchingAndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }

    @Override
    public DispatchingAndroidInjector<BroadcastReceiver> broadcastReceiverInjector(){
        return dispatchingBroadcastInector;
    }

    @Override
    public DispatchingAndroidInjector<Fragment> fragmentInjector(){
        return dispatchingFragmentInjector;
    }

}
