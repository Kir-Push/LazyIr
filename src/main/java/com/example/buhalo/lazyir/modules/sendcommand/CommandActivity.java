package com.example.buhalo.lazyir.modules.sendcommand;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;

/**
 * Created by buhalo on 07.04.17.
 */

public class CommandActivity extends AppCompatActivity{
    private ListView entries;
    private  ArrayAdapter<String> adapter;
  //  private ArrayAdapter<String> secondLevelAdapter;
  //  private Button buttonBack;
    private Button buttonAdd;
    private int btnId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        if(b != null)
            btnId = b.getInt("btnId");
        setContentView(R.layout.ir_activity);
        entries = (ListView) findViewById(R.id.entries);
        List<String> fileList = DBHelper.getInstance(this).getCommandsPc();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,fileList);
      //  secondLevelAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,fileList);
        entries.setAdapter(adapter);
        entries.setChoiceMode(CHOICE_MODE_MULTIPLE);
      //  entries.setOnItemClickListener(this);
        Button  buttonBack = (Button) findViewById(R.id.ir_btn_back);
        buttonBack.setVisibility(View.INVISIBLE);
        buttonAdd = (Button)findViewById(R.id.ir_btn_add);
       // buttonAdd.setVisibility(View.INVISIBLE);
        setButtonAddListener();
      //  setButtonBackListener();
        setResult(RESULT_OK,new Intent().putExtra("btnId",btnId));

    }

    private void setButtonBackListener() {
//        buttonBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(deph >=3)
//                {
//                    deph = 2; //tk v onitemClick v nachale inckrement na odin tut na odin menwe chem nado
//                    //    onItemClick(null,null,secondLevelPos,1);
//                    List<String> list = DBHelper.getInstance(v.getContext().getApplicationContext()).getTypeByProducer(secondLevel);
//                    entries.setChoiceMode(CHOICE_MODE_SINGLE);
//                    buttonBack.setVisibility(View.VISIBLE);
//                    entries.setAdapter(adapter);
//                    adapter.clear();
//                    adapter.addAll(list);
//                    adapter.notifyDataSetChanged();
//                    buttonAdd.setVisibility(View.INVISIBLE);
//                }
//                else if(deph == 2)
//                {
//                    deph = 1;
//                    List<String>  list =  DBHelper.getInstance(v.getContext().getApplicationContext()).getProducerList();
//                    entries.setChoiceMode(CHOICE_MODE_SINGLE);
//                    buttonBack.setVisibility(View.INVISIBLE);
//                    entries.setAdapter(adapter);
//                    adapter.clear();
//                    adapter.addAll(list);
//                    adapter.notifyDataSetChanged();
//                    buttonAdd.setVisibility(View.INVISIBLE);
//                    //   onItemClick(null,null,firstLevelPos,1);
//                }
//            }
//        });
    }

    private void addCommand(String id,Set<String> commands) {
        for(String cmd : commands)
        {
            DBHelper.getInstance(getApplicationContext()).saveBtnCommand(cmd,id);
        }
    }

    private void setButtonAddListener() {
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray sbArray = entries.getCheckedItemPositions();
                Set<String> save = new HashSet<String>();
                for(int i = 0;i<sbArray.size();i++)
                {
                    int key = sbArray.keyAt(i);
                    if (sbArray.get(key)) {
                        save.add(adapter.getItem(key));
                    }
//                    if (sbArray.get(key)) {
//                        DBHelper.getInstance(getApplicationContext()).saveBtnCommandTemp( adapter.getItem(key),secondLevel,thirdLevel,btnId);
//                    }

                }
                addCommand(Integer.toString(btnId),save);
            }
        });
    }


}
