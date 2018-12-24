package com.example.buhalo.lazyir.di;

import com.example.buhalo.lazyir.AppBoot;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

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
