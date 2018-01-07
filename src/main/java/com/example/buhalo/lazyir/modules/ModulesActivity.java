package com.example.buhalo.lazyir.modules;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;

/**
 * Created by buhalo on 06.11.17.
 */

public class ModulesActivity extends AppCompatActivity {

    private ListView moduleListView;
    private ModuleSettingAdapter moduleSettingAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.modules_setting);
        moduleListView = findViewById(R.id.module_list);

        moduleSettingAdapter = new ModuleSettingAdapter(this,ModuleFactory.getModulesNamesWithStatus(this));
        moduleListView.setAdapter(moduleSettingAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
