package com.example.buhalo.lazyir.di;


import com.example.buhalo.lazyir.di.scope.ActivityScope;
import com.example.buhalo.lazyir.view.activity.CommandActivity;
import com.example.buhalo.lazyir.view.activity.MainActivity;
import com.example.buhalo.lazyir.view.activity.ModulesActivity;
import com.example.buhalo.lazyir.view.fragment.PageFragment;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Module(includes = {AndroidSupportInjectionModule.class})
public interface MainActivityModule {

    @ActivityScope
    @ContributesAndroidInjector()
    MainActivity mainActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector()
    CommandActivity commandActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector()
    ModulesActivity modulesActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector()
    PageFragment modulesPageFragmentInjector();
}
