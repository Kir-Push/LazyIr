package com.example.buhalo.lazyir.modules.shareManager;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;

import java.io.File;
import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

public class ShareActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener,AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener,AbsListView.MultiChoiceModeListener {

    private ShareModule module;
    private  FolderAdater android_adapter;
    private FolderAdater server_adapter;
    private ListView android_list;

    private ListView pcFileList;

    private String lastPath;


    private  String currPath;

    private String lastPathS;

    private String currPaths;

    private int tabSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_page);
        TabLayout tabs = (TabLayout) findViewById(R.id.share_tabs);
        pcFileList = (ListView) findViewById(R.id.pc_file_list);
        android_list = (ListView) findViewById(R.id.file_list);
        tabs.addOnTabSelectedListener(this);
        Device device = Device.getConnectedDevices().get(MainActivity.getSelected_id());
        if(device != null)
        module = (ShareModule) device.getEnabledModules().get(ShareModule.class.getSimpleName());
        if(module == null || device == null)
        {
            CharSequence text = "No connection!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
            return;
        }
        List<FileWrap> filesList = module.getFilesList(module.getRootPath());
        List<FileWrap> serverFilesList = module.getFilesListFromServer("root");
        lastPath = module.getRootPath();
        currPath = lastPath;
        lastPathS = "/"; // todo for test (you need create windwos version
        currPaths = lastPathS;
        android_adapter = new FolderAdater(this,filesList);
        android_list.setAdapter(android_adapter);
        server_adapter = new FolderAdater(this,serverFilesList);
        pcFileList.setAdapter(server_adapter);
        findViewById(R.id.share_ok).setVisibility(View.INVISIBLE);
       // android_list.setOnItemLongClickListener(this);
        android_list.setOnItemClickListener(this);
        pcFileList.setOnItemClickListener(new ServerClickListener());
        registerForContextMenu(pcFileList);
        registerForContextMenu(android_list);
        tabs.getTabAt(0).select();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        android_adapter.setCbSend(true);
        server_adapter.setCbSend(true);
        findViewById(R.id.share_ok).setVisibility(View.VISIBLE);
        android_adapter.notifyDataSetChanged();
        server_adapter.notifyDataSetChanged();
       android_list.setOnItemClickListener(null);
        return super.onContextItemSelected(item);
    }

    public void onOkclick(View view)
    {
        android_adapter.notifyDataSetChanged();
        server_adapter.notifyDataSetChanged();
        android_list.setOnItemClickListener(this);
        if(tabSelected == 1) {
            module.startDownloading(getCurrPath(), getCurrPaths(), server_adapter.getChecked());
        }else if(tabSelected == 0)
        {
            module.startSending(getCurrPaths(),getCurrPath(),android_adapter.getChecked());
        }
        server_adapter.setCbSend(false);
        android_adapter.setCbSend(false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    //    menu.setHeaderTitle(android_adapter.getFile(info.position).getPath());
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
            tabSelected = 0;
            android_list.setVisibility(View.VISIBLE);
        }
        else if(tab.getPosition() == 1)
        {
            tabSelected = 1;
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
        FileWrap item = android_adapter.getFile(position);
        android_adapter.clear();
        String temp = getCurrPath()+"/"+item.getPath();
        if(position == 0)
        {
            System.out.println(position + "  Position");
            setCurrPath(getLastPath());
            if(getLastPathS() != null && !getLastPath().equals(module.getRootPath()))
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

        List<FileWrap> fileList = module.getFilesList(getCurrPath());
        android_adapter.setFileWrapList(fileList);
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

    public String getLastPathS() {
        return lastPathS;
    }

    public void setLastPathS(String lastPathS) {
        this.lastPathS = lastPathS;
    }

    public String getCurrPaths() {
        return currPaths;
    }

    public void setCurrPaths(String currPaths) {
        this.currPaths = currPaths;
    }


    public class ServerClickListener implements AdapterView.OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            System.out.println("dadada");
            FileWrap item = server_adapter.getFile(position);
            server_adapter.clear();
            String temp = getCurrPaths()+"/"+item.getPath();
            if(position == 0)
            {
                System.out.println(position + "  Position");
                setCurrPaths(getLastPathS());
                if(getLastPathS() != null && !getLastPathS().equals(module.getRootPathFromServer()))
                {
                    int i = getLastPathS().lastIndexOf("/");
                    String substring;
                    if(i > 1) {
                        substring = getLastPathS().substring(0, i);
                    }
                    else
                    {
                        substring = "/";
                    }
                    System.out.println("Substing path " + substring);
                    setLastPathS(substring);
                }

            }
            else if(item.isFile())
            {
                //   setCurrPath(getLastPath());
            }
            else
            {
                setLastPathS(getCurrPaths());
                setCurrPaths(temp);
            }

            List<FileWrap> fileList = module.getFilesListFromServer(getCurrPaths());
            server_adapter.setFileWrapList(fileList);
            server_adapter.notifyDataSetChanged();
        }
    }
}
