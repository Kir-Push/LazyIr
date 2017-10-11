package com.example.buhalo.lazyir.modules.dbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by buhalo on 15.04.17.
 */

public class MediaRemoteActivity extends AppCompatActivity {

    private HashMap<String,Player> playerHashMap;
    private ArrayAdapter<String>  adapter;
    private List<String> playerNameToAdapter;
    private Mpris module;

    private int selectedPlayer;

    private Button play;
    private SeekBar timeLine;
    private TextView title;
    private TextView lenghtTxt;
    private SeekBar volumeLine;
    private Button next;
    private Button prev;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    private Deque<DbusCommand> commandQueque = new ArrayDeque<>();
    private final DbusCommand cachedSendGetAll = new DbusCommand(1, null, 0, null, null);
    private final DbusCommand cachedGetPlayers = new DbusCommand(7, null, 0, null, null);
    private static volatile boolean pendingUserAction = false;

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            try {
                DbusCommand cmd;
                List<Player> players;
                if(commandQueque.size() == 0) {
                    commandQueque.push(cachedSendGetAll); // in end put send and get to intent, these command needs to be executed every time !
                    commandQueque.push(cachedGetPlayers);
                }
                while ((cmd = commandQueque.pollLast()) != null) {
                    System.out.println("AAAAAA!!!   " + cmd);
                    switch (cmd.getCode()) {
                        case 1:
                            module.sendGetAllPlayers();
                            break;
                        case 2:
                            module.sendSeek(cmd.getWhom(), cmd.getArg());
                            break;
                        case 3:
                            module.sendVolume(cmd.getWhom(), cmd.getArg());
                            break;
                        case 4:
                            module.sendNext(cmd.getWhom());
                            break;
                        case 5:
                            module.sendPlayPause(cmd.getWhom());
                            break;
                        case 6:
                            module.sendPrev(cmd.getWhom());
                            break;
                        case 7:
                            pendingUserAction = false;
                            players = module.getPlayers(3000);
                            fillPlayers(players);
                            break;
                        default:
                            break;
                    }
                }
            }catch (InterruptedException e)
            {
                //todo
            }

        }
    };

    private void fillPlayers(List<Player> players) throws InterruptedException {
        if(pendingUserAction) {
            pendingUserAction = false;
            return;}
        if(players == null) return;

        playerNameToAdapter.clear();
        int counter = 0;
        for(Player player : players)
        {
            String name;
            if (player.getName().startsWith("js9876528:")) {     // some number to identify browser or dbus
                name = player.getTitle();
                while(playerNameToAdapter.contains(name))
                    name += "-"  +String.valueOf(++counter);
            }
            else {
                int length = player.getName().length();
                name = player.getName().substring(23,length);
            }
            playerNameToAdapter.add(name);
            playerHashMap.put(name,player);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateState();
            }
        });
    }

    private void updateState() {
        if(playerNameToAdapter.size() == 0)
            return;
        if(selectedPlayer >= playerNameToAdapter.size())
            selectedPlayer = 0;
        adapter.notifyDataSetChanged();
        Player player = getPlayer();

        timeLine.setMax((int) player.getLenght());
        timeLine.setProgress((int) player.getCurrTime());
        timeLine.refreshDrawableState();

       setVolumeLine((int) player.getVolume());

        title.setText(player.getTitle());
        title.refreshDrawableState();

        lenghtTxt.setText(player.getReadyTimeString());
        lenghtTxt.refreshDrawableState();

        if(player.getPlaybackStatus().equals("\"Playing\""))
            play.setBackgroundResource(R.mipmap.pause_btn);
        else
            play.setBackgroundResource(R.mipmap.play_btn);
        play.refreshDrawableState();
    }

    private void setVolumeLine(int volume)
    {
        volumeLine.setMax(100);
        volumeLine.setProgress(volume);
        volumeLine.refreshDrawableState();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Device.getConnectedDevices().size() == 0) {
            Toast.makeText(this,"No connection",Toast.LENGTH_SHORT).show();
            finish();
            return;}
        setContentView(R.layout.media_control);

        module = (Mpris) Device.getConnectedDevices().get(MainActivity.selected_id).getEnabledModules().get(Mpris.class.getSimpleName());
        playerNameToAdapter = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playerNameToAdapter);
        playerHashMap = new HashMap<>();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {selectedPlayer = position;}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }});

        timeLine = (SeekBar) findViewById(R.id.timeLine);
        title = (TextView) findViewById(R.id.trackName);
        lenghtTxt = (TextView) findViewById(R.id.lenght_text);
        play = (Button) findViewById(R.id.playButton);
        volumeLine = (SeekBar)  findViewById(R.id.volume_lin).findViewById(R.id.volume_bar);
        next = (Button) findViewById(R.id.nextBtn);
        prev = (Button) findViewById(R.id.prewBtn);
        setListeners();
    }



    //start task on ui because you need interact with ui,period: every 500? or 250 ms
    @Override
    protected void onStart() {
        super.onStart();
        scheduledFuture = scheduler.scheduleAtFixedRate(task ,0, 800, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearTimer();
    }

    //this method run when activity stop, clear timer and erase all resources
    @Override
    protected void onStop() {
        super.onStop();
        clearTimer();
    }

    // close task and itterrupt if still running;
    private void clearTimer()
    {
        if(scheduledFuture != null)
        scheduledFuture.cancel(true);
    }


    private void setListeners() {
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putCommand(5, "play",0);
            }});
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putCommand(4, "next",0);
            }});
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putCommand(6, "prev",0);
            }});
        volumeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                putCommand(3,"volume",seekBar.getProgress());
                setVolumeLine(seekBar.getProgress());}});

        timeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Player player = getPlayer();
                int arg = 0;
                if(player.getName().startsWith("js9876528:")) // some number to identify browser or dbus
                    arg = seekBar.getProgress();
                else
                    arg = (int) (seekBar.getProgress() - player.getCurrTime());
               putCommand(2,"seek",arg); }});

    }

    private Player getPlayer()
    {
        return playerHashMap.get(playerNameToAdapter.get(selectedPlayer));
    }

    private String getPlayerName()
    {
        return getPlayer().getName();
    }


    private void putCommand(int i,String commandName,int arg)
    {
        if(selectedPlayer >= playerNameToAdapter.size()) return;
        try {commandQueque.push(new DbusCommand(i, null, arg, null, getPlayerName())); pendingUserAction = true;
        } catch (Exception e) {Log.e("MediaRemoteActivity","Error when put command "+ commandName + " in commandQueque!");}
    }



}
