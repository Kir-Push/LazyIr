package com.example.buhalo.lazyir;

/**
 * Created by buhalo on 08.01.17.
 */

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("tag1");

        tabSpec.setIndicator("Вкладка 1");

        tabSpec.setContent(R.id.tab1);

        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");

        tabSpec.setIndicator("Вкладка 2");

        tabSpec.setContent(R.id.tab2);

        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag3");

        tabSpec.setIndicator("Вкладка 3");

        tabSpec.setContent(R.id.tab3);

        tabHost.addTab(tabSpec);

        tabHost.setCurrentTabByTag("tag1");

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Toast.makeText(getBaseContext(),"tab=1",Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void processClickButton(View view)
    {
      IrMethods.processOnlyAudio(view,this);
        IrMethods.processOnlyTv(view,this);
    }

    public void processOnlyTv(View view)
    {
        IrMethods.processOnlyTv(view,this);
    }

    public void processOnlyAudio(View view)
    {
        IrMethods.processOnlyAudio(view,this);
    }

    public void processOnlyPc(View view){IrMethods.processOnlyPC(view,this);}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
