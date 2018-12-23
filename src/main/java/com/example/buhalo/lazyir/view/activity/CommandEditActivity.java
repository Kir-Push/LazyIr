package com.example.buhalo.lazyir.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.sendcommand.Command;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;

public class CommandEditActivity extends AppCompatActivity {
    @Inject
    @Setter
    @Getter
    DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Command selectedCommand = intent.getParcelableExtra("cmd");
        setContentView(R.layout.command_edit);
        EditText editName = findViewById(R.id.editCommandName);
        EditText editText = findViewById(R.id.editCommandText);
        Button cancelButton = findViewById(R.id.cancelButtonCommand);
        Button saveButton = findViewById(R.id.saveButtonCommand);
        if(selectedCommand != null){
            editName.setText(selectedCommand.getCommandName());
            editText.setText(selectedCommand.getCmd());
        }
        cancelButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> {
            if(selectedCommand != null){
                selectedCommand.setCmd(editText.getText().toString());
                selectedCommand.setCommandName(editName.getText().toString());
                dbHelper.updateCommand(selectedCommand);
            }else{
                Command command = new Command(editName.getText().toString(),editText.getText().toString(),1);
                dbHelper.saveCommand(command);
            }
            finish();
        });
    }
}
