package com.example.buhalo.lazyir.modules.dbus;

import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 15.04.17.
 */

public class Mpris extends Module {

    public final static String FIRST_PART_DEST = "dbus-send --session --dest=";
    public final static String FREEDESKTOP_DBUS = "org.freedesktop.DBus";
    public final static String MEDIUM_PART = "--type=method_call --print-reply /org/mpris/MediaPlayer2 ";
    public final static String GET_ALL_MPRIS = FIRST_PART_DEST +  FREEDESKTOP_DBUS + " --type=method_call --print-reply /org/freedesktop/DBus org.freedesktop.DBus.ListNames | grep org.mpris.MediaPlayer2";
    public final static String PLAYER_INTERFACE = "org.mpris.MediaPlayer2.Player";
    public final static String SEEK = ".Seek int64:";
    public final static String NEXT = ".Next";
    public final static String PREVIOUS = ".Previous";
    public final static String PLAYPAUSE = ".PlayPause";
    public final static String STOP = ".Stop";
    public final static String OPENURI = ".OpenUri string:";
    public final static String SETPOSITION = ".SetPosition Object Path:";
    public final static String VOLUME = ".Volume double:";
    public final static String PLAYBACKSTATUS = ".PlaybackStatus string:";
    public final static String MONITOR = "dbus-monitor --session \"path=/org/mpris/MediaPlayer2,member=PropertiesChanged\" --monitor ";
    public final static String PLAYER = "player";
    public final static String GET_ALL_INFO = "allInfo";
    public final static String ALL_PLAYERS = "allPlayers";

    private List<Player> players = new ArrayList<>();


    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data)
        {
            case ALL_PLAYERS:
                fillPlayers(np);
                break;
            default:
                break;

        }
    }

    private void fillPlayers(NetworkPackage np) {
        players.clear();
        players.addAll(np.getObject(ALL_PLAYERS,Players.class).getPlayerList());
    }

    public List<Player> getPlayers()
    {
        sendGetAllPlayers();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Log.e(Mpris.class.getSimpleName(),e.toString());
        }
        return players;
    }

    public void sendMetadata(String player)
    {
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),GET_ALL_INFO);
        np.setValue(PLAYER,player);
        sendMsg(np.getMessage());
    }

    public void sendPlayPause(String player)
    {
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),PLAYPAUSE);
        np.setValue(PLAYER,player);
        sendMsg(np.getMessage());
    }

    public void sendNext(String player)
    {
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),NEXT);
        np.setValue(PLAYER,player);
        sendMsg(np.getMessage());
    }

    public void sendVolume(String player,int volume)
    {
        double vol = ((double)volume)/100;
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),VOLUME);
        np.setValue(VOLUME,Double.toString(vol));
        np.setValue(PLAYER,player);
        sendMsg(np.getMessage());
    }

    public void sendSeek(String player, int seek) {
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),SEEK);
        np.setValue(SEEK,Integer.toString(seek));
        np.setValue(PLAYER,player);
        sendMsg(np.getMessage());
    }

    public void sendPrev(String player)
    {
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),PREVIOUS);
        np.setValue(PLAYER,player);
        sendMsg(np.getMessage());
    }



    private void sendGetAllPlayers()
    {
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),ALL_PLAYERS);
        sendMsg(np.getMessage());
    }
}
