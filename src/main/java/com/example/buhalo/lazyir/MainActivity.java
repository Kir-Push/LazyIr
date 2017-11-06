package com.example.buhalo.lazyir;

/**
 * Created by buhalo on 08.01.17.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.UI.SampleFragmentPagerAdapter;
import com.example.buhalo.lazyir.modules.ModulesActivity;
import com.example.buhalo.lazyir.old.IrMethods;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.buhalo.lazyir.UI.PageFragment.NonEditListenerTouch;


public class MainActivity extends AppCompatActivity {


    public static String selected_id = "";
    final String LOG_TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DevicesAdapter adapter;
    private SampleFragmentPagerAdapter sampleFragmentPagerAdapter;

    public static boolean isEditMode() {
        return editMode;
    }

    private static transient boolean editMode = false;

    private static final int PERMISSION_STORAGE = 567;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_STORAGE);


        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.mipmap.andrr);
        actionBar.setTitle(selected_id);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        adapter = new DevicesAdapter(this);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.openDrawer,  /* "open drawer" description */
                R.string.close_drawer  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

         sampleFragmentPagerAdapter = new SampleFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this);
                ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(sampleFragmentPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

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
        // Create a new fragment and specify the planet to show based on position
       selected_id = ((Device)adapter.getItem(position)).getId();
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
     //   setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateOptionsMenu();
         adapter.notifyDataSetChanged();
         getSupportActionBar().setTitle(selected_id);

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if(item.toString().equals("Edit"))
        {
            boolean checked = item.isChecked();
            item.setChecked(!checked);
            editMode = !checked;
            // when selected option edit, buttons need to change listeners, therefore need recreate fragment's.
            // crutch is re-set fragment adapter to layout, which cause recreating fragments
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
           viewPager.setAdapter(sampleFragmentPagerAdapter);
        }else if(item.toString().equals("Options"))
        {
            Intent intent = new Intent(this, ModulesActivity.class);
            startActivity(intent);
        }else
        if(Device.getConnectedDevices().get(selected_id).isPaired() && item.toString().equals("Unpair"))
       {
           TcpConnectionManager.getInstance().unpair(selected_id,this);
       }
       else if (!Device.getConnectedDevices().get(selected_id).isPaired() && item.toString().equals("Pair"))
       {
           TcpConnectionManager.getInstance().sendPairing(selected_id);
       }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {



        if(Device.getConnectedDevices().get(selected_id) != null && Device.getConnectedDevices().get(selected_id).isPaired())
        {
            menu.add("Unpair");
        }
        else
        {
            menu.add("Pair");
        }
        menu.add("Options");
        MenuItem edit = menu.add("Edit");
        edit.setCheckable(true);
        if(editMode) {
            edit.setChecked(true);
        }
        else {
            edit.setChecked(false);
        }
        return true;
    }



    private class DevicesAdapter extends BaseAdapter
    {
        Context ctx;
        LayoutInflater lInflater;


        public DevicesAdapter(Context ctx) {
            this.ctx = ctx;
            lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return Device.getConnectedDevices().size();
        }

        @Override
        public Object getItem(int position) {
            int i = 0;
            for(Device dv : Device.getConnectedDevices().values())
            {
                if(i == position)
                {
                   return dv;
                }
                i++;
            }
            return null;
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
            ((TextView) view.findViewById(R.id.connected_device)).setText(d.getId());
            if(d.isPaired())
            ((ImageView) view.findViewById(R.id.pair_status)).setImageResource(R.mipmap.yes_pair);
            else
            {
                ((ImageView) view.findViewById(R.id.pair_status)).setImageResource(R.mipmap.no_pair);
            }
            if(selected_id.equals(d.getId()))
            {
                ((TextView) view.findViewById(R.id.connected_device)).setTextColor(Color.GRAY);
            }
            return view;
        }

        Device getProduct(int position) {
            return ((Device) getItem(position));
        }
    }
}
