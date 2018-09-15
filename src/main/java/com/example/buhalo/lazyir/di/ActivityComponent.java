package com.example.buhalo.lazyir.di;


import com.example.buhalo.lazyir.AppBoot;
import com.example.buhalo.lazyir.di.scope.ActivityScope;
import com.example.buhalo.lazyir.view.activity.MainActivity;

import dagger.Component;
import dagger.Subcomponent;

@Subcomponent(modules = MainActivityModule.class)
public interface ActivityComponent {
    void inject(AppBoot appBoot);

}
