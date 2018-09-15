package com.example.buhalo.lazyir.view.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.view.adapters.ModuleSettingAdapter;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;


public class ModulesActivity extends AppCompatActivity {

    @Inject @Setter @Getter
    ModuleFactory moduleFactory;
    @Inject @Setter @Getter
    DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.modules_setting);
        ListView moduleListView = findViewById(R.id.module_list);
        ModuleSettingAdapter moduleSettingAdapter = new ModuleSettingAdapter(this,dbHelper, BackgroundUtil.getModulesWithStatus());
        moduleListView.setAdapter(moduleSettingAdapter);
    }


}
