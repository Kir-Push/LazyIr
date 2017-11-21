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
// ServerAnswer LinkedList are queque for received players from server,
// with size only 1
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
    public final static String REPEAT = "repeat";

    private final static int TIMEOUT = 1000;
    private LinkedBlockingQueue<List<Player>> serverAnswer = new LinkedBlockingQueue<>(1);


    @Override
    public void execute(NetworkPackage np) {
        try {
        switch (np.getData())
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
            Log.e("Mpris","Error in Mpris execute",e);
        }
    }

    // use offer insteab pull, because i don't want to wait while list was empty,
    // and after some time, these data may be outdate, so if list full, just wait
    // #TIMEOUT second's and put or ignore
    private void fillPlayers(NetworkPackage np) throws InterruptedException {
        serverAnswer.offer(np.getObject(ALL_PLAYERS,Players.class).getPlayerList(),TIMEOUT,TimeUnit.MILLISECONDS);
    }

    // wait and return null if time exceeds
    List<Player> getPlayers(int timeout) throws InterruptedException {
        return serverAnswer.poll(timeout, TimeUnit.MILLISECONDS);
    }

    void sendMetadata(String player) {
        send(player,GET_ALL_INFO);
    }

    void sendPlayPause(String player) {
        send(player,PLAYPAUSE);
    }

    void sendNext(String player) {
        send(player,NEXT);
    }

    void sendVolume(String player,int volume) {
        double vol = ((double)volume)/100;
        sendMsg(NetworkPackage.Cacher.getOrCreatePackage(Mpris.class.getSimpleName(),SEEK).setValue(PLAYER,player).setValue(VOLUME,Double.toString(vol)).getMessage());
    }

    // hard to read, but one string ;)
    // in send methods, it get get networkPackage from cache, set Player, set method's now return this networkPackage, so
    // after set other value if need and finally call getMessage.
    void sendSeek(String player, int seek) {
        sendMsg( NetworkPackage.Cacher.getOrCreatePackage(Mpris.class.getSimpleName(),SEEK).setValue(SEEK,Integer.toString(seek)).setValue(PLAYER,player).getMessage());
    }

    void sendPrev(String player) {
        send(player,PREVIOUS);
    }

    private void send(String player,String cmd){
        sendMsg(NetworkPackage.Cacher.getOrCreatePackage(Mpris.class.getSimpleName(),cmd).setValue(PLAYER,player).getMessage());
    }

    void sendRepeat(String player){
        sendMsg(NetworkPackage.Cacher.getOrCreatePackage(Mpris.class.getSimpleName(), REPEAT).setValue(PLAYER,player).getMessage());
    }

    // Mpris actually doesn't have any state
    @Override
    public void endWork() {
        serverAnswer = null;
    }

    void sendGetAllPlayers() {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(Mpris.class.getSimpleName(),ALL_PLAYERS);
        sendMsg(np.getMessage());
    }

    public LinkedBlockingQueue<List<Player>> getServerAnswer() {
        return serverAnswer;
    }

    public void clearPlayers() {
        serverAnswer.clear();
    }
}
