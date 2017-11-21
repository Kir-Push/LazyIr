package com.example.buhalo.lazyir.modules;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.shareManager.ShareActivity;

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
//        if(MainActivity.selected_id == null || MainActivity.selected_id.equals("") || Device.getConnectedDevices().size() == 0) {
//            CharSequence text = "No connection! ";
//            int duration = Toast.LENGTH_SHORT;
//            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
//            toast.show();
//            finish();
//            return;
//        } // todo commented for test, when tested using mock device
        // mock device for test purposes
      //  MainActivity.setSelected_id("dadada");
      //  Device.getConnectedDevices().put("dadada",new Device(null,"dadada","agasjka",null,null,null,this));


        Device device = Device.getConnectedDevices().get(MainActivity.getSelected_id());
        if(device == null){
            Toast toast = Toast.makeText(getApplicationContext(), "Sorry no connection",Toast.LENGTH_SHORT );
            toast.show();
            finish();
        }
        moduleSettingAdapter = new ModuleSettingAdapter(this,ModuleFactory.getModulesNamesWithStatus(device,this));
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
