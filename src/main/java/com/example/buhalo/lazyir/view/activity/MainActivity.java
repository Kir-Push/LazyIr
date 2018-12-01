package com.example.buhalo.lazyir.view.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.bus.events.MainActivityCommand;
import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.service.network.tcp.PairService;
import com.example.buhalo.lazyir.view.UiCmds;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collection;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DevicesAdapter adapter;
    @Inject @Getter @Setter
    PairService pairService;

    private static final int PERMISSION_ALL = 566;
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_CALL_LOG,Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECEIVE_SMS,Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE,Manifest.permission_group.PHONE};


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void commandFromBackend(MainActivityCommand cmd){
        if(cmd.getCommand().equals(UiCmds.UPDATE_ACTIVITY)){
            updateActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        requestPermissions();


        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.android_app);
            actionBar.setTitle(BackgroundUtil.getSelectedId());
        }
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);
        adapter = new DevicesAdapter(getApplicationContext());
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener((parent, view, position, id) -> selectItem(position));

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.openDrawer,  /* "open drawer" description */
                R.string.close_drawer  /* "close drawer" description */
        );
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        createMainPage();
    }

    private void createMainPage() {
        LinearLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.findViewById(R.id.media_start_btn).setOnClickListener(v -> {
            Intent intent = new Intent(this, MediaRemoteActivity.class);
            startActivity(intent);
        });
        contentFrame.findViewById(R.id.touch_control).setOnClickListener(v -> {
            Intent intent = new Intent(this, TouchActivity.class);
            startActivity(intent);
        });
        contentFrame.findViewById(R.id.command_start_btn).setOnClickListener(v -> {
            Intent intent = new Intent(this, CommandActivity.class);
            startActivity(intent);
        });
        contentFrame.findViewById(R.id.share_start_btn).setOnClickListener(v -> {

        });
        contentFrame.findViewById(R.id.clipboard_start_btn).setOnClickListener(v -> {

        });
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                PERMISSIONS,
                PERMISSION_ALL);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectItem(int position) {
        // Create a new fragment and specify the device to show based on position
        // Highlight the selected item, update the title, and close the drawer
        Device item = (Device) adapter.getItem(position);
        if(item != null) {
            BackgroundUtil.setSelectedId(item.getId());
            mDrawerList.setItemChecked(position, true);
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null) {
            supportActionBar.setTitle(BackgroundUtil.getSelectedId());
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        Device device = BackgroundUtil.getDevice(BackgroundUtil.getSelectedId());
      if(item.toString().equals("Options")) {
            Intent intent = new Intent(this, ModulesActivity.class);
            startActivity(intent);
        }else if(device != null && device.isPaired() && item.toString().equals("UnPair")) {
            pairService.sendUnpairRequest(device.getId(),getApplicationContext());
       } else if (device != null && !device.isPaired() && item.toString().equals("Pair")) {
            pairService.sendPairRequest(device.getId(),getApplicationContext());
       }
        invalidateOptionsMenu();
        adapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Device device = BackgroundUtil.getDevice(BackgroundUtil.getSelectedId());
        if(device != null && device.isPaired()) {
            menu.add("UnPair");
        } else {
            menu.add("Pair");
        }
        menu.add("Options");
        return true;
    }

    public DevicesAdapter getAdapter() {
        return adapter;
    }

    private void updateActivity(){
        invalidateOptionsMenu();
        getAdapter().notifyDataSetChanged();
    }



    private class DevicesAdapter extends BaseAdapter {
        Context ctx;
        LayoutInflater lInflater;


        DevicesAdapter(Context ctx) {
            this.ctx = ctx;
            lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return BackgroundUtil.getConnectedDevices().size();
        }

        @Override
        public Object getItem(int position) {
            Collection<Device> values = BackgroundUtil.getConnectedDevices().values();
            if(values.size() <= position){
                return values.iterator().next();
            }
           return values.toArray(new Device[0])[position];
        }

        @Override
        public long getItemId(int position) {
         return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = lInflater.inflate(R.layout.pair_item, parent, false);
            }
            Device d = getProduct(position);
            if(d != null) {
                String id = d.getId();
                ((TextView) view.findViewById(R.id.connected_device)).setText(id);
                if (d.isPaired()) {
                    ((ImageView) view.findViewById(R.id.pair_status)).setImageResource(R.drawable.yes_pair);
                }
                else {
                    ((ImageView) view.findViewById(R.id.pair_status)).setImageResource(R.drawable.no_pair);
                }
                if (BackgroundUtil.getSelectedId().equals(id)) {
                    ((TextView) view.findViewById(R.id.connected_device)).setTextColor(Color.GRAY);
                }
            }
            return view;
        }

        Device getProduct(int position) {
            return ((Device) getItem(position));
        }

    }
}
