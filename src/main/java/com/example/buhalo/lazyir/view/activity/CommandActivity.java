package com.example.buhalo.lazyir.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;

public class CommandActivity extends AppCompatActivity{
    private ListView entries;
    private  ArrayAdapter<String> adapter;
    private Button buttonAdd;
    private int btnId;

    @Inject @Setter @Getter
    DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            btnId = b.getInt("btnId");
        }
        setContentView(R.layout.ir_activity);
        entries = findViewById(R.id.entries);
        List<String> fileList = dbHelper.getCommandsPc();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, fileList);
        entries.setAdapter(adapter);
        entries.setChoiceMode(CHOICE_MODE_MULTIPLE);
        Button  buttonBack = findViewById(R.id.ir_btn_back);
        buttonBack.setVisibility(View.INVISIBLE);
        buttonAdd = findViewById(R.id.ir_btn_add);
        setButtonAddListener();
        setResult(RESULT_OK,new Intent().putExtra("btnId",btnId));
    }



    private void addCommand(String id,Set<String> commands) {
        Stream.of(commands).forEach(cmd ->dbHelper.saveBtnCommand(cmd,id));
    }

    private void setButtonAddListener() {
        buttonAdd.setOnClickListener(v -> {
            SparseBooleanArray sbArray = entries.getCheckedItemPositions();
            Set<String> save = new HashSet<>();
            for(int i = 0;i<sbArray.size();i++) {
                int key = sbArray.keyAt(i);
                if (sbArray.get(key)) {
                    save.add(adapter.getItem(key));
                }
            }
            addCommand(Integer.toString(btnId),save);
        });
    }


}
