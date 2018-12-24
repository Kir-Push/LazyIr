package com.example.buhalo.lazyir.di;


import com.example.buhalo.lazyir.AppBoot;
import dagger.Subcomponent;

@Subcomponent(modules = MainActivityModule.class)
public interface ActivityComponent {
    void inject(AppBoot appBoot);

}
