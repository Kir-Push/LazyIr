package com.example.buhalo.lazyir.modules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.R;

import java.util.List;

/**
 * Created by buhalo on 06.11.17.
 */

public class ModuleSettingAdapter extends BaseAdapter {

    LayoutInflater lInflater;
    List<ModulesWrap> modulesList;

    public ModuleSettingAdapter(Context ctx, List<ModulesWrap> modulesList) {
        this.lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.modulesList = modulesList;
    }

    public List<ModulesWrap> getModulesList() {
        return modulesList;
    }

    public void setModulesList(List<ModulesWrap> modulesList) {
        this.modulesList = modulesList;
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
        CheckBox cbSend = (CheckBox) view.findViewById(R.id.share_ckBox);
        cbSend.setOnCheckedChangeListener(myCheckChangeList);
        cbSend.setTag(position);
        cbSend.setChecked(module.isStatus());
        return view;
    }

    CompoundButton.OnCheckedChangeListener myCheckChangeList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // меняем данные товара (в корзине или нет) // в какой блять корзине, ты откуда это взял? а пох впринципе работает и ладно
           ModulesWrap modulesWrap = ((ModulesWrap)getItem((Integer) buttonView.getTag()));
           modulesWrap.setStatus(isChecked);
            String moduleName = modulesWrap.getModuleName();
            ModuleFactory.changeModuleStatus(Device.getConnectedDevices().get(modulesWrap.getDvId()), moduleName,lInflater.getContext(),isChecked);
            // todo hanbdle change status, turn on or off module,
        }
    };
}
