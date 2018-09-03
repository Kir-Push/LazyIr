package com.example.buhalo.lazyir.di;

import android.app.Application;
import android.content.Context;

import com.example.buhalo.lazyir.AppBoot;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;

@Singleton
@Component(modules = {ServiceModule.class})
public interface AppComponent  {

    @Component.Builder
    interface Builder {
        @BindsInstance
        AppComponent.Builder application(AppBoot application);
        AppComponent build();
    }

    ModuleComponent getModuleComponent();

    ServiceComponent getServiceComponent();

}
