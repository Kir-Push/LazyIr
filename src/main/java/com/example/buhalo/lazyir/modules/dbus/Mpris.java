package com.example.buhalo.lazyir.modules.dbus;

import android.util.Log;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by buhalo on 15.04.17.
 */

public class Mpris extends Module {

    public final static String SEEK = "seek";
    public final static String NEXT = "next";
    public final static String PREVIOUS = "previous";
    public final static String PLAYPAUSE = "playPause";
    public final static String STOP = "stop";
    public final static String OPENURI = "openUri";
    public final static String SETPOSITION = "setPosition";
    public final static String VOLUME = "volume";
    public final static String PLAYER = "player";
    public final static String GET_ALL_INFO = "allInfo";
    public final static String ALL_PLAYERS = "allPlayers";



    private double counter = 0;
    private double lastReturnCounter = -1;


    private LinkedBlockingQueue<List<Player>> serverAnswer = new LinkedBlockingQueue<>(1);


    @Override
    public void execute(NetworkPackage np) {
        try {
        String data = np.getData();
        switch (data)
        {
            case ALL_PLAYERS:
                    fillPlayers(np);
                break;
            case "UnsupportedOS":
                Toast.makeText(device.getContext(),"Sorry " + np.getValue("OS") + " not support this function",Toast.LENGTH_LONG).show();
                break;
            default:
                break;

        }
        } catch (InterruptedException e) {
            Log.e("Mpris",e.toString());
        }
    }

    private void fillPlayers(NetworkPackage np) throws InterruptedException {
   //     if(serverAnswer.size() > 0)
   //         serverAnswer.clear();
        serverAnswer.put(np.getObject(ALL_PLAYERS,Players.class).getPlayerList());
        counter++;
    }

    public List<Player> getPlayers(int timeout) throws InterruptedException {

        return serverAnswer.poll(timeout, TimeUnit.MILLISECONDS);
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



    public void sendGetAllPlayers()
    {
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),ALL_PLAYERS);
        sendMsg(np.getMessage());
    }

    public LinkedBlockingQueue<List<Player>> getServerAnswer() {
        return serverAnswer;
    }

    public void clearPlayers() {
        serverAnswer.clear();
    }
}
