package com.example.buhalo.lazyir.di;


import dagger.Subcomponent;

@Subcomponent(modules = AppModule.class)
public interface ServiceComponent {
    ActivityComponent getActivityComponent();
}
