package com.example.buhalo.lazyir.modules.shareManager;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

public class ShareActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener,AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener,AbsListView.MultiChoiceModeListener {

    private ShareModule module;
    private  ArrayAdapter<String> android_adapter;
    private ListView android_list;

    private ListView pcFileList;

    private String lastPath;


    private  String currPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_page);
        TabLayout tabs = (TabLayout) findViewById(R.id.share_tabs);
        pcFileList = (ListView) findViewById(R.id.pc_file_list);
        android_list = (ListView) findViewById(R.id.file_list);
        tabs.addOnTabSelectedListener(this);
//        module = (ShareModule) Device.connectedDevices.get(MainActivity.selected_id).getEnabledModules().get(ShareModule.class.getSimpleName()); // todo only for test
        module = new ShareModule();
        List<File> filesList = module.getFilesList(module.getRootPath());
        List<String> toAdapter = new ArrayList<>();
        lastPath = module.getRootPath();
        currPath = lastPath;
        toAdapter.add("....");
        for(File f : filesList)
        {
            toAdapter.add(f.getName());
        }
        android_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item,toAdapter);
        android_list.setAdapter(android_adapter);
       // android_list.setOnItemLongClickListener(this);
        android_list.setOnItemClickListener(this);
        System.out.println("i'here2");

       // android_list.setMultiChoiceModeListener(this);
        registerForContextMenu(pcFileList);
        registerForContextMenu(android_list);
        tabs.getTabAt(0).select();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle(android_adapter.getItem(info.position));
        String[] menuItems = {"Send","Select"};
        for (int i = 0; i<menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    public String getLastPath() {
        return lastPath;
    }

    public String getCurrPath() {
        return currPath;
    }
    public void setLastPath(String lastPath) {
        this.lastPath = lastPath;
    }

    public void setCurrPath(String currPath) {
        this.currPath = currPath;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if(tab.getPosition() == 0)
        {
            android_list.setVisibility(View.VISIBLE);
        }
        else if(tab.getPosition() == 1)
        {
            pcFileList.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if(tab.getPosition() == 0)
        {
            android_list.setVisibility(View.INVISIBLE);
        }
        else if(tab.getPosition() == 1)
        {
            pcFileList.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = android_adapter.getItem(position);
        android_adapter.clear();
        String temp = getCurrPath()+"/"+item;
        if(position == 0)
        {
            System.out.println(position + "  Position");
            setCurrPath(getLastPath());
            if(!getLastPath().equals(module.getRootPath()))
            {
                int i = getLastPath().lastIndexOf("/");
                String substring = getLastPath().substring(0, i);
                System.out.println("Substing path " + substring);
                setLastPath(substring);
            }

        }
        else if(new File(temp).isFile())
        {
            //   setCurrPath(getLastPath());
        }
        else
        {
            setLastPath(getCurrPath());
            setCurrPath(temp);
        }

        List<File> fileList = module.getFilesList(getCurrPath());
        List<String> toAdapter = new ArrayList<>();
        toAdapter.add("....");
        for(File f : fileList)
        {
            toAdapter.add(f.getName());
        }
        android_adapter.addAll(toAdapter);
        android_adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
