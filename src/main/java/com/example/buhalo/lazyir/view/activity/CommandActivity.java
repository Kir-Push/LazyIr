package com.example.buhalo.lazyir.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.view.adapters.CommandsAdapter;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;


public class CommandActivity extends AppCompatActivity{

    @Inject @Setter @Getter
    DBHelper dbHelper;
    private String selectedId;
    CommandsAdapter moduleSettingAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        selectedId = BackgroundUtil.getSelectedId();
        Device device = BackgroundUtil.getDevice(selectedId);
        if(device == null || !BackgroundUtil.hasActualConnection()) {
            Toast.makeText(this,"No connection",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.commands_layout);
        ListView commandListView = findViewById(R.id.command_list_view);
        moduleSettingAdapter = new CommandsAdapter(this,dbHelper,dbHelper.getCommandFull(),selectedId);
        commandListView.setAdapter(moduleSettingAdapter);
        Button addCmdButton = findViewById(R.id.addNewCommandBtn);
        addCmdButton.setOnClickListener(v ->{
            Intent intent = new Intent(this, CommandEditActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        moduleSettingAdapter.notifyDataSetChanged();
    }
}
