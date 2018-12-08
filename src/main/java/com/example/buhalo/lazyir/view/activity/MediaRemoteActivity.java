package com.example.buhalo.lazyir.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.modules.dbus.Mpris;
import com.example.buhalo.lazyir.modules.dbus.MprisCommand;
import com.example.buhalo.lazyir.modules.dbus.MprisDto;
import com.example.buhalo.lazyir.modules.dbus.Player;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MediaRemoteActivity extends AppCompatActivity {

    private HashMap<String,Player> playerHashMap;
    private ArrayAdapter<String>  adapter;
    private List<String> playerNameToAdapter;

    private int selectedPlayer;
    private String selectedId;

    private Button play;
    private SeekBar timeLine;
    private TextView title;
    private TextView lenghtTxt;
    private SeekBar volumeLine;
    private Button next;
    private Button prev;
    private Button repeat;
    private boolean timeTouched;
    private boolean volumeTouched;

    private ScheduledFuture<?> scheduledFuture;


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receivePlayers(MprisDto mprisDto){
        TreeSet<Player> players = new TreeSet<>(mprisDto.getPlayers());
        if(players.isEmpty()){
            return;
        }
        playerNameToAdapter.clear();
        for (Player player : players) {
            String name = setPlayerName(player);
            playerNameToAdapter.add(name);
            playerHashMap.put(name,player);
        }

        if(playerNameToAdapter.isEmpty()) {
            return;
        }
        if(selectedPlayer >= playerNameToAdapter.size()) {
            selectedPlayer = 0;
        }

        adapter.notifyDataSetChanged();
        Player player = getPlayer();
        if(player != null) {
            double length = player.getLength();
            double currTime = player.getCurrTime();
            setTimeLine(length,currTime);
            timeLine.refreshDrawableState();

            lenghtTxt.setText(createTimeStatusString((int)currTime, (int)length));
            lenghtTxt.refreshDrawableState();

            setVolumeLine(player.getVolume());

            title.setText(player.getTitle());
            title.refreshDrawableState();
            String status = player.getStatus();
            if (status.equalsIgnoreCase("Playing") || status.equalsIgnoreCase("\"Playing\"")) {
                play.setBackgroundResource(R.drawable.pause);
            } else {
                play.setBackgroundResource(R.drawable.play);
            }
            play.refreshDrawableState();
        }
    }

    private void setTimeLine(double length, double currTime) {
        if(!timeTouched) {
            if (length != currTime && length > 0 && currTime >= 0) {
                timeLine.setMax((int) length);
                timeLine.setProgress((int) currTime);
            } else {
                timeLine.setMax(0);
                timeLine.setProgress(0);
            }
        }else{
            timeTouched = false;
        }

    }

    private String setPlayerName(Player player) {
        int counter = 1;
        StringBuilder name = new StringBuilder(player.getName());
        StringBuilder temp = new StringBuilder(name);
        while(playerNameToAdapter.contains(temp.toString())) {
            temp = new StringBuilder(name);
           temp.append(" ").append(counter++);
        }

        if(player.getId().equals("-1")){
            int length = player.getName().length();
            name = new StringBuilder(player.getName().substring(23, length));
        }
        return name.toString();
    }

    private String createTimeStatusString(int currTime, int length) {
        String result = "";
        if(currTime < 0 && length == currTime){
            result += currTime;
            return result;
        }
        int secs = currTime % 60;
        int min = currTime / 60;
        if(min < 10 && min >= 0){
            result += "0";
        }
        result += Integer.toString(min);
        result += ":";
        if(secs < 10 && secs >= 0){
            result += "0";
        }
        result += Integer.toString(secs);
        result += " / ";
        int lengSecs = length % 60;
        int lengMin = length / 60;
        if(lengMin < 10 && lengMin >= 0){
            result += "0";
        }
        result += Integer.toString(lengMin);
        result += ":";
        if(lengSecs < 10 && lengSecs >= 0){
            result += "0";
        }
        result += Integer.toString(lengSecs);
        return result;
    }

    private void setVolumeLine(double volume) {
        int actualVolume = (int)volume;
        if(actualVolume <= 0){
            actualVolume = (int) (volume * 100);
        }
        if(!volumeTouched) {
            volumeLine.setMax(100);
            volumeLine.setProgress(actualVolume);
            volumeLine.refreshDrawableState();
        }else{
            volumeTouched = false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedId = BackgroundUtil.getSelectedId();
        Device device = BackgroundUtil.getDevice(selectedId);
        if(device == null || !BackgroundUtil.hasActualConnection()) {
            Toast.makeText(this,"No connection",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.media_control);
        playerNameToAdapter = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playerNameToAdapter);
        playerHashMap = new HashMap<>();

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {selectedPlayer = position;}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //hz what to do here
            }});

        timeLine =  findViewById(R.id.timeLine);
        title =  findViewById(R.id.trackName);
        lenghtTxt = findViewById(R.id.lenght_text);
        play = findViewById(R.id.playButton);
        volumeLine = findViewById(R.id.volume_bar);
        next =  findViewById(R.id.nextBtn);
        prev = findViewById(R.id.prewBtn);
        repeat = findViewById(R.id.loop_btn);

        setListeners();
    }

    private void setListeners() {
        play.setOnClickListener(v -> EventBus.getDefault().post(new MprisCommand(Mpris.api.PLAYPAUSE.name(),selectedId,getPlayer())));
        next.setOnClickListener(v -> EventBus.getDefault().post(new MprisCommand(Mpris.api.NEXT.name(),selectedId,getPlayer())));
        prev.setOnClickListener(v -> EventBus.getDefault().post(new MprisCommand(Mpris.api.PREVIOUS.name(),selectedId,getPlayer())));
        repeat.setOnClickListener(v -> EventBus.getDefault().post(new MprisCommand(Mpris.api.REPEAT.name(),selectedId,getPlayer())));
        volumeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //nothing to do here
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //nothing to do here
                 }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                volumeTouched = true;
                EventBus.getDefault().post(new MprisCommand(Mpris.api.VOLUME.name(),selectedId,getPlayer(),seekBar.getProgress()));
            }});

        timeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //nothing to do here
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //nothing to do here
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Player player = getPlayer();
                int arg;
                if(!player.getId().equals("-1")) {
                    arg = seekBar.getProgress();
                }
                else {
                    arg = (int) (seekBar.getProgress() - player.getCurrTime());
                }
                timeTouched = true;
                EventBus.getDefault().post(new MprisCommand(Mpris.api.SEEK.name(),selectedId,player,arg));
            }
        });

    }



    //start task on ui because you need interact with ui,period: every 500? or 250 ms -- WAT?
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        scheduledFuture = BackgroundUtil.getTimerExecutor().scheduleWithFixedDelay(
                () -> EventBus.getDefault().post(new MprisCommand(Mpris.api.ALLPLAYERS.name(),selectedId)),
                0, 400, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearTimer();
    }

    //this method run when activity stop, clear timer and erase all resources
    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        clearTimer();
        super.onStop();
    }

    private void clearTimer() {
        if(scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    private Player getPlayer() {
        return playerHashMap.get(playerNameToAdapter.get(selectedPlayer));
    }



}
