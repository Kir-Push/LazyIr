package com.example.buhalo.lazyir.modules.dbus;

import android.content.Context;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


public class Mpris extends Module {
    public enum api{
        SEEK,
        NEXT,
        PREVIOUS,
        STOP,
        PLAYPAUSE,
        OPENURI,
        SETPOSITION,
        VOLUME,
        ALLPLAYERS
    }

    @Inject
    public Mpris(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void execute(NetworkPackage np) {
        MprisDto dto = (MprisDto) np.getData();
        if (dto.getCommand().equals(api.ALLPLAYERS.name())) {
            fillPlayers(dto);
        }
    }

    private void fillPlayers(MprisDto dto) {
        EventBus.getDefault().post(dto);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void receiveCommandFromActivity(MprisCommand cmd){
        if(!cmd.getId().equals(device.getId())){
            return;
        }
        api command =  api.valueOf(cmd.getCommand());
       switch (command){
           case ALLPLAYERS:
               sendGetAllPlayers();
               break;
           case NEXT:
               sendNext(cmd.getPlayer());
               break;
           case SEEK:
               sendSeek(cmd.getPlayer(),cmd.getData());
               break;
           case STOP:
               sendPlayPause(cmd.getPlayer());
               break;
           case VOLUME:
               sendVolume(cmd.getPlayer(),cmd.getData());
               break;
           case PREVIOUS:
               sendPrev(cmd.getPlayer());
               break;
           case PLAYPAUSE:
               sendPlayPause(cmd.getPlayer());
               break;
           case SETPOSITION:
               sendSeek(cmd.getPlayer(),cmd.getData());
               break;
           default:
               break;
       }
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
        context = null;
        device = null;
    }

    private void sendGetAllPlayers() {
        String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, new MprisDto(api.ALLPLAYERS.name()));
        sendMsg(message);
    }

    private void sendPlayPause(Player player) {
        send(player,api.PLAYPAUSE.name());
    }

    private void sendNext(Player player) {
        send(player,api.NEXT.name());
    }

    private void sendVolume(Player player,int volume) {
        double vol = ((double)volume)/100;
        send(player,api.VOLUME.name(),vol);
    }

    private void sendSeek(Player player, int seek) {
        send(player,api.SEEK.name(),seek);
    }

    private void send(Player player,String cmd){
        send(player,cmd,0);
    }

    private void sendPrev(Player player) {
        send(player,api.PREVIOUS.name());
    }

    private void send(Player player,String cmd,double data){
        MprisDto mprisDto = new MprisDto(cmd);
        String name = player.getName();
        if(!player.getId().equals("-1")){
            mprisDto.setPlayerType("browser");
            mprisDto.setJsIp(player.getIp());
            mprisDto.setJsId(player.getId());
        }else{
            mprisDto.setPlayerType("desktop");
        }
        String[] split = name.split("-!!-");
        if(split.length == 0){
            mprisDto.setPlayer(name);
        }else{
            mprisDto.setPlayer(split[0]);
        }
        mprisDto.setDValue(data);
        String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, mprisDto);
        sendMsg(message);
    }
}
