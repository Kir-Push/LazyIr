package com.example.buhalo.lazyir.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.view.adapters.CommandsAdapter;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;


public class CommandActivity extends AppCompatActivity{

    @Inject @Setter @Getter
    DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.commands_layout);
        ListView commandListView = findViewById(R.id.command_list_view);
        CommandsAdapter moduleSettingAdapter = new CommandsAdapter(this,dbHelper,dbHelper.getCommandFull());
        commandListView.setAdapter(moduleSettingAdapter);
    }


}
