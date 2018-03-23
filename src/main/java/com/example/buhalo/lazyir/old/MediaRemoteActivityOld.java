//package com.example.buhalo.lazyir.old;
//
///**
// * Created by buhalo on 05.10.17.
// */
//
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.SeekBar;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.buhalo.lazyir.Devices.Device;
//import com.example.buhalo.lazyir.MainActivity;
//import com.example.buhalo.lazyir.R;
//import com.example.buhalo.lazyir.modules.dbus.Mpris;
//import com.example.buhalo.lazyir.modules.dbus.Player;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.SeekBar;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.buhalo.lazyir.Devices.Device;
//import com.example.buhalo.lazyir.MainActivity;
//import com.example.buhalo.lazyir.R;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//
///**
// * Created by buhalo on 15.04.17.
// */
//@Deprecated
//public class MediaRemoteActivityOld extends AppCompatActivity {
//
//    private List<Player> players;
//    private List<String> playerNameToAdapter;
//    private HashMap<String,Player> playerHashMap;
//    private ArrayAdapter<String> adapter;
//    private Mpris module;
//
//    private int selectedPlayer;
//    private Timer playerTimer;
//    private Timer metadataTimer;
//
//    private Button play;
//    private SeekBar timeLine;
//    private TextView title;
//    private TextView lenghtTxt;
//    private SeekBar volumeLine;
//
//    private static boolean waitToAnswer = false;
//
//
//    // -1 is false, other value is expected value from server;
//    private int changedManuallyTime = -1;
//    private int countchangedTime = 0;
//    private int changedManuallyVolume = -1;
//    private int countchangedVolume = 0;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if(Device.getConnectedDevices().size() == 0) {
//            Toast.makeText(this,"No connection",Toast.LENGTH_SHORT).show();
//
//            finish();
//            return;
//        }
//        setContentView(R.layout.media_control);
//        playerHashMap = new HashMap<>();
//        module = (Mpris) Device.getConnectedDevices().get(MainActivity.selected_id).getEnabledModules().get(Mpris.class.getSimpleName());
//        playerNameToAdapter = new ArrayList<>();
//        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playerNameToAdapter);
//        Spinner spinner = (Spinner) findViewById(R.id.spinner);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedPlayer = position;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        timeLine = (SeekBar) findViewById(R.id.timeLine);
//        title = (TextView) findViewById(R.id.trackName);
//        lenghtTxt = (TextView) findViewById(R.id.lenght_text);
//        play = (Button) findViewById(R.id.playButton);
//        Button next = (Button) findViewById(R.id.nextBtn);
//        Button prev = (Button) findViewById(R.id.prewBtn);
//        View layout = findViewById(R.id.volume_lin);
//        volumeLine = (SeekBar) layout.findViewById(R.id.volume_bar);
//        play.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(selectedPlayer >= playerNameToAdapter.size())
//                    return;
//                module.sendPlayPause(playerHashMap.get(playerNameToAdapter.get(selectedPlayer)).getName());
//            }
//        });
//        next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(selectedPlayer >= playerNameToAdapter.size())
//                    return;
//                module.sendNext(playerHashMap.get(playerNameToAdapter.get(selectedPlayer)).getName());
//            }
//        });
//        prev.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(selectedPlayer >= playerNameToAdapter.size())
//                    return;
//                module.sendPrev(playerHashMap.get(playerNameToAdapter.get(selectedPlayer)).getName());
//            }
//        });
//
//        volumeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if(selectedPlayer >= playerNameToAdapter.size())
//                    return;
//
//                waitToAnswer = true;
//                updateTimer();
//                changedManuallyVolume = seekBar.getProgress();
//                Player player = playerHashMap.get(playerNameToAdapter.get(selectedPlayer));
//                module.sendVolume(player.getName(),seekBar.getProgress());
//                player.setVolume(seekBar.getProgress());
//            }
//        });
//
//        timeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if(selectedPlayer >= playerNameToAdapter.size() )
//                    return;
//
//                waitToAnswer = true;
//                module.clearPlayers();
//                clearTimer();
//                changedManuallyTime = seekBar.getProgress();
//                Player player = playerHashMap.get(playerNameToAdapter.get(selectedPlayer));
//                if(player.getName().startsWith("js9876528:")) // some number to identify browser or dbus
//                {
//                    module.sendSeek(player.getName(), seekBar.getProgress()); //todo try to do in dbus similar
//                }
//                else {
//                    module.sendSeek(player.getName(), (int) (seekBar.getProgress() - player.getCurrTime()));
//                }
//                player.setCurrTime(seekBar.getProgress());
//                playersAvailableCheck();
//            }
//        });
//
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        playersAvailableCheck();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        clearTimer();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        clearTimer();
//    }
//
//    private void clearTimer()
//    {
//        if(playerTimer != null) {
//            playerTimer.cancel();
//            playerTimer.purge();
//        }
//    }
//
//
//
//    private TimerTask getTimerTask()
//    {
//        return   new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(MainActivity.selected_id.equals("") || !Device.getConnectedDevices().containsKey(MainActivity.selected_id))
//                        {
//                            cancel();
//                            finish();
//
//                        }
////                        if(waitToAnswer) //todo think about it's not best decision i think, think about rewrite all class and you need new architecture of this module and class
////                        {
////                            waitToAnswer = false;
////                            return;
////                        }
//                        players = module.getPlayers();
//                        if(players.size() == 0)
//                        {
//                            return;
//                        }
//                        playerNameToAdapter.clear();
//                        //   playerHashMap.clear();
//                        int counter = 0;
//                        for(Player player : players)
//                        {
//                            String name;
//                            if(player.getName().startsWith("js9876528:")) // some number to identify browser or dbus
//                            {
//                                name = player.getTitle();
//                                while(playerNameToAdapter.contains(name))
//                                {
//                                    name += "-"  +String.valueOf(++counter);
//                                }
//                            }
//                            else {
//                                name = player.getName().substring(23, player.getName().length());
//                            }
//                            playerNameToAdapter.add(name); // todo here actually you need handle browser input- now it will show id?
//                            playerHashMap.put(name,player);
//                        }
//                        updateState();
//                    }
//                });
//            }
//        };
//    }
//    private void playersAvailableCheck() {
//
//        playerTimer =  new Timer();
//        playerTimer.schedule(getTimerTask(),0,700);
//    }
//
//    public void updateTimer(){
//        module.clearPlayers();
//        clearTimer();
//        playerTimer = new Timer();
//        playerTimer.schedule(getTimerTask(),0, 1000);
//    }
//
//    private void updateState() {
//
//        if(playerNameToAdapter.size() == 0)
//            return;
//
//        if(selectedPlayer >= playerNameToAdapter.size())
//            selectedPlayer = 0;
//
//        adapter.notifyDataSetChanged();
//        Player player = playerHashMap.get(playerNameToAdapter.get(selectedPlayer));
//
//        countchangedTime++;
//        countchangedVolume++;
//
////        double timetemp = player.getCurrTime() - changedManuallyTime;
////        if((timetemp <= 1000 && timetemp >= -1000 ) || countchangedTime >= 3)
////        {
////            changedManuallyTime = -1;
////            countchangedTime = 0;
////        }
////        if(player.getVolume() == changedManuallyVolume || countchangedVolume >= 3)
////        {
////            changedManuallyVolume = -1;
////            countchangedVolume = 0;
////        }
//
//        //    if(changedManuallyTime == -1) {
//
//        //   }
//        // if(changedManuallyVolume == -1) {
//        if(!waitToAnswer) {
//            timeLine.setMax((int) player.getLenght());
//            timeLine.setProgress((int) player.getCurrTime());
//            timeLine.refreshDrawableState();
//            volumeLine.setMax(100);
//            volumeLine.setProgress((int) player.getVolume());
//            volumeLine.refreshDrawableState();
//        }
//        else
//        {
//            waitToAnswer = false;
//        }
//        //  }
//
//        title.setText(player.getTitle());
//        title.refreshDrawableState();
//        lenghtTxt.setText(player.getReadyTimeString());
//        lenghtTxt.refreshDrawableState();
//        if(player.getPlaybackStatus().equals("\"Playing\""))
//        {
//            play.setBackgroundResource(R.mipmap.pause_btn);
//        }
//        else
//        {
//            play.setBackgroundResource(R.mipmap.play_btn);
//        }
//        play.refreshDrawableState();
//    }
//
//}
