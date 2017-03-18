package com.example.buhalo.lazyir;

/**
 * Created by buhalo on 08.01.17.
 */

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.buhalo.lazyir.UI.SampleFragmentPagerAdapter;
import com.example.buhalo.lazyir.old.IrMethods;


public class MainActivity extends AppCompatActivity {


    public static String selected_id = "";

    final String LOG_TAG = "MainActivity";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       // TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

      //  tabHost.setup();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);




//
//        startService(new Intent(this,BaseService.class).setAction("StartListening"));
//        startService(new Intent(this,BaseService.class).setAction("sendBroadcast"));

        //startService(new Intent(this,BaseService.class));

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

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

    public void processOnlyVolumeUp(View view){IrMethods.increaseVolume(view,this);}

    public void processOnlyVolumeDown(View view){IrMethods.decreaseVolume(view,this);}

    public void processBroadcast(View view){IrMethods.sendBroadcast(view,this);}

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
