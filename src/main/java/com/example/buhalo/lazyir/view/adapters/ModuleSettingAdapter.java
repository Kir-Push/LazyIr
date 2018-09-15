package com.example.buhalo.lazyir.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.ModulesWrap;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import java.util.List;


public class ModuleSettingAdapter extends BaseAdapter {


    private LayoutInflater lInflater;
    private List<ModulesWrap> modulesList;
    private DBHelper dbHelper;

    public ModuleSettingAdapter(Context ctx, DBHelper dbHelper, List<ModulesWrap> modulesList) {
        this.lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.modulesList = modulesList;
        this.dbHelper = dbHelper;
    }

    @Override
    public int getCount() {
        return modulesList.size();
    }

    @Override
    public Object getItem(int position) {
        return modulesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.share_item, parent, false);
        }
        ModulesWrap module = (ModulesWrap) getItem(position);
        ((TextView)view.findViewById(R.id.share_fldr_name)).setText(module.getModuleName());
        CheckBox cbSend = view.findViewById(R.id.share_ckBox);
        cbSend.setOnCheckedChangeListener(myCheckChangeList);
        cbSend.setTag(position);
        cbSend.setChecked(module.isStatus());
        return view;
    }

    CompoundButton.OnCheckedChangeListener myCheckChangeList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           ModulesWrap modulesWrap = ((ModulesWrap)getItem((Integer) buttonView.getTag()));
           modulesWrap.setStatus(isChecked);
            String moduleName = modulesWrap.getModuleName();
            if(isChecked){
                BackgroundUtil.enableModule(moduleName,dbHelper);
            }else{
                BackgroundUtil.disableModule(moduleName,dbHelper);
            }
        }
    };
}
