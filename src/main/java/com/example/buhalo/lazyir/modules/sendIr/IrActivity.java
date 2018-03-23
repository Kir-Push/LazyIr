package com.example.buhalo.lazyir.modules.sendIr;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.R;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;
import static android.widget.AbsListView.CHOICE_MODE_SINGLE;

/**
 * Created by buhalo on 02.04.17.
 */

public class IrActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView entries;
    private int deph;
    private  ArrayAdapter<String> adapter;
    private String secondLevel;
    private String thirdLevel;
    private Button buttonBack;
    private Button buttonAdd;
    private int btnId = 0;
    private ArrayAdapter<String> thirdLevelAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        if(b != null)
            btnId = b.getInt("btnId");
        setContentView(R.layout.ir_activity);
        deph = 1;
        entries = (ListView) findViewById(R.id.entries);
        List<String> fileList = DBHelper.getInstance(this).getProducerList();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, fileList);
        thirdLevelAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,fileList);
        entries.setAdapter(adapter);
        entries.setOnItemClickListener(this);
        buttonBack = (Button) findViewById(R.id.ir_btn_back);
        buttonAdd = (Button)findViewById(R.id.ir_btn_add);
        buttonBack.setVisibility(View.INVISIBLE);
        buttonAdd.setVisibility(View.INVISIBLE);
        setButtonAddListener();
        setButtonBackListener();
        setResult(RESULT_OK,new Intent().putExtra("btnId",btnId));


    }

    private void setButtonBackListener() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deph >=3)
                {
                    deph = 2; //tk v onitemClick v nachale inckrement na odin tut na odin menwe chem nado
                //    onItemClick(null,null,secondLevelPos,1);
                   List<String> list = DBHelper.getInstance(v.getContext().getApplicationContext()).getTypeByProducer(secondLevel);
                    entries.setChoiceMode(CHOICE_MODE_SINGLE);
                    buttonBack.setVisibility(View.VISIBLE);
                    entries.setAdapter(adapter);
                    adapter.clear();
                    adapter.addAll(list);
                    adapter.notifyDataSetChanged();
                    buttonAdd.setVisibility(View.INVISIBLE);
                }
                else if(deph == 2)
                {
                    deph = 1;
                    List<String>  list =  DBHelper.getInstance(v.getContext().getApplicationContext()).getProducerList();
                    entries.setChoiceMode(CHOICE_MODE_SINGLE);
                    buttonBack.setVisibility(View.INVISIBLE);
                    entries.setAdapter(adapter);
                    adapter.clear();
                    adapter.addAll(list);
                    adapter.notifyDataSetChanged();
                    buttonAdd.setVisibility(View.INVISIBLE);
                 //   onItemClick(null,null,firstLevelPos,1);
                }
            }
        });
    }

    private void setButtonAddListener() {
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray sbArray = entries.getCheckedItemPositions();
                for(int i = 0;i<sbArray.size();i++)
                {
                    int key = sbArray.keyAt(i);
                    if (sbArray.get(key)) {
                        DBHelper.getInstance(getApplicationContext()).saveBtnCommandTemp( thirdLevelAdapter.getItem(key),secondLevel,thirdLevel,btnId);
                    }

                }
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(deph == 4)
        {
            System.out.println("ja tut");
            return;
        }
        if(deph < 4)
        {
            deph++;
        }
        List<String> list = null;
        String entry;
        if(adapter.getCount() > 0) {
            entry = adapter.getItem(position);
        }else
        {
            entry = secondLevel;
        }
        switch (deph)
        {
            case 1:
            list =  DBHelper.getInstance(this).getProducerList();
                entries.setChoiceMode(CHOICE_MODE_SINGLE);
                buttonBack.setVisibility(View.INVISIBLE);
                entries.setAdapter(adapter);
                break;
            case 2:
                list = DBHelper.getInstance(this).getTypeByProducer(entry);
                entries.setChoiceMode(CHOICE_MODE_SINGLE);
                secondLevel = entry;
                buttonBack.setVisibility(View.VISIBLE);
                entries.setAdapter(adapter);
                break;
            case 3:
                list = DBHelper.getInstance(this).getCodeNamesByType(entry,secondLevel);
                entries.setChoiceMode(CHOICE_MODE_MULTIPLE);
                thirdLevel = entry;
                thirdLevelAdapter.clear();
                thirdLevelAdapter.addAll(list);
                entries.setAdapter(thirdLevelAdapter);
                thirdLevelAdapter.notifyDataSetChanged();
                buttonBack.setVisibility(View.VISIBLE);
                buttonAdd.setVisibility(View.VISIBLE);
                break;
            default:
                list = new ArrayList<>();
                break;
        }
        if(!entry.equals(""))
        {

        }
        if(deph < 3) {
            adapter.clear();
            adapter.addAll(list);
            adapter.notifyDataSetChanged();
            buttonAdd.setVisibility(View.INVISIBLE);
        }
    }
}
