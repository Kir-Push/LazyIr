package com.example.buhalo.lazyir.modules.dbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by buhalo on 15.04.17.
 */

public class MediaRemoteActivity extends AppCompatActivity {

    private  List<Player> players;
    private List<String> playerNameToAdapter;
    private HashMap<String,Player> playerHashMap;
    private ArrayAdapter<String>  adapter;
    private Mpris module;

    private int selectedPlayer;
    private Timer playerTimer;
    private Timer metadataTimer;

    private Button play;
    private SeekBar timeLine;
    private TextView title;
    private TextView lenghtTxt;
    private SeekBar volumeLine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Device.getConnectedDevices().size() == 0) {
            Toast.makeText(this,"No connection",Toast.LENGTH_SHORT).show();

            finish();
            return;
        }
        setContentView(R.layout.media_control);
        playerHashMap = new HashMap<>();
        module = (Mpris) Device.getConnectedDevices().get(MainActivity.selected_id).getEnabledModules().get(Mpris.class.getSimpleName());
        playerNameToAdapter = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playerNameToAdapter);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPlayer = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timeLine = (SeekBar) findViewById(R.id.timeLine);
        title = (TextView) findViewById(R.id.trackName);
        lenghtTxt = (TextView) findViewById(R.id.lenght_text);
        play = (Button) findViewById(R.id.playButton);
        Button next = (Button) findViewById(R.id.nextBtn);
        Button prev = (Button) findViewById(R.id.prewBtn);
        View layout = findViewById(R.id.volume_lin);
        volumeLine = (SeekBar) layout.findViewById(R.id.volume_bar);
           play.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(selectedPlayer >= playerNameToAdapter.size())
                       return;
                   module.sendPlayPause(playerHashMap.get(playerNameToAdapter.get(selectedPlayer)).getName());
               }
           });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedPlayer >= playerNameToAdapter.size())
                    return;
                module.sendNext(playerHashMap.get(playerNameToAdapter.get(selectedPlayer)).getName());
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedPlayer >= playerNameToAdapter.size())
                    return;
                module.sendPrev(playerHashMap.get(playerNameToAdapter.get(selectedPlayer)).getName());
            }
        });

        volumeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(selectedPlayer >= playerNameToAdapter.size())
                    return;
                Player player = playerHashMap.get(playerNameToAdapter.get(selectedPlayer));
                module.sendVolume(player.getName(),seekBar.getProgress());
                player.setVolume(seekBar.getProgress());
            }
        });

        timeLine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(selectedPlayer >= playerNameToAdapter.size() )
                    return;
                Player player = playerHashMap.get(playerNameToAdapter.get(selectedPlayer));
                module.sendSeek(player.getName(),seekBar.getProgress()-player.getCurrTime());
                player.setCurrTime(seekBar.getProgress());
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        playersAvailableCheck();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(playerTimer != null) {
            playerTimer.cancel();
            playerTimer.purge();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(playerTimer != null) {
            playerTimer.cancel();
            playerTimer.purge();
        }
    }

    private void playersAvailableCheck() {
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(MainActivity.selected_id.equals("") || !Device.getConnectedDevices().containsKey(MainActivity.selected_id))
                        {
                            cancel();
                            finish();

                        }
                players = module.getPlayers();
                playerNameToAdapter.clear();
              //   playerHashMap.clear();
                for(Player player : players)
                {
                    String name = player.getName().substring(23,player.getName().length());
                    playerNameToAdapter.add(name);
                    playerHashMap.put(name,player);
                }
                        updateState();
                    }
                });
            }
        };
        playerTimer = new java.util.Timer();
        playerTimer.schedule(tt,0,800);
    }

    private void updateState() {

                if(playerNameToAdapter.size() == 0)
                    return;

        if(selectedPlayer >= playerNameToAdapter.size())
            selectedPlayer = 0;

        adapter.notifyDataSetChanged();
        Player player = playerHashMap.get(playerNameToAdapter.get(selectedPlayer));
        timeLine.setMax(player.getLenght());
        timeLine.setProgress(player.getCurrTime());
        timeLine.refreshDrawableState();
        volumeLine.setMax(100);
        volumeLine.setProgress(player.getVolume());
        volumeLine.refreshDrawableState();
        title.setText(player.getTitle());
        title.refreshDrawableState();
        lenghtTxt.setText(player.getReadyTimeString());
        lenghtTxt.refreshDrawableState();
        if(player.getPlaybackStatus().equals("\"Playing\""))
        {
            play.setBackgroundResource(R.mipmap.pause_btn);
        }
        else
        {
            play.setBackgroundResource(R.mipmap.play_btn);
        }
        play.refreshDrawableState();
    }

}
