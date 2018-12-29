package com.example.buhalo.lazyir.view.activity;

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
import com.example.buhalo.lazyir.view.adapters.ClipboardAdapter;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;

public class ClipboardActivity extends AppCompatActivity {

    @Inject @Setter @Getter
    DBHelper dbHelper;
    ClipboardAdapter clipboardAdater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        String selectedId = BackgroundUtil.getSelectedId();
        Device device = BackgroundUtil.getDevice(selectedId);
        if(device == null || !BackgroundUtil.hasActualConnection()) {
            Toast.makeText(this,"No connection",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.clipboard_layout);
        ListView commandListView = findViewById(R.id.clipboard_list);
        clipboardAdater = new ClipboardAdapter(this,dbHelper,dbHelper.getClipboardFull());
        commandListView.setAdapter(clipboardAdater);
        Button clearAllBtn = findViewById(R.id.eraseClipboardBtn);
        clearAllBtn.setOnClickListener(v ->{
         dbHelper.clearAllClipboard();
         clipboardAdater.notifyChanged();
        });
    }

    //todo timer which reccurent update datasetChanged
    @Override
    protected void onResume() {
        super.onResume();
        clipboardAdater.notifyDataSetChanged();
    }
}
