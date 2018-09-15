package com.example.buhalo.lazyir.di;


import com.example.buhalo.lazyir.di.scope.ActivityScope;
import com.example.buhalo.lazyir.di.scope.Service;

import dagger.Component;
import dagger.Subcomponent;

@Subcomponent(modules = AppModule.class)
public interface ServiceComponent {
    ActivityComponent getActivityComponent();
}
