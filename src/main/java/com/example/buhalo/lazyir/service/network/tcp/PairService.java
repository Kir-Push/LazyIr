package com.example.buhalo.lazyir.service.network.tcp;

import android.content.Context;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.bus.events.MainActivityCommand;
import com.example.buhalo.lazyir.modules.share.ShareModuleCommand;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.modules.ModuleCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.service.dto.TcpDto;
import com.example.buhalo.lazyir.view.GuiCommunicator;
import com.example.buhalo.lazyir.view.UiCmds;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

public class PairService {

    private DBHelper dbHelper;
    private GuiCommunicator guiCommunicator;
    private MessageFactory messageFactory;

    @Inject
    public PairService(DBHelper dbHelper, GuiCommunicator guiCommunicator, MessageFactory messageFactory) {
        this.dbHelper = dbHelper;
        this.guiCommunicator = guiCommunicator;
        this.messageFactory = messageFactory;
    }

    public void sendPairRequest(String id, Context context){
        String message = messageFactory.createMessage(TcpConnectionManager.api.TCP.name(), false, new TcpDto(TcpConnectionManager.api.PAIR.name(), Integer.toString(BackgroundUtil.getMyId().hashCode())));
        BackgroundUtil.sendToDevice(id,message,context);
    }

    public void sendUnpairRequest(String id,Context context){
        String message = messageFactory.createMessage(TcpConnectionManager.api.TCP.name(), false, new TcpDto(TcpConnectionManager.api.UNPAIR.name()));
        BackgroundUtil.sendToDevice(id,message,context);
        unPairDevice(id);
    }

    private void unPairDevice(String id) {
        Device device = BackgroundUtil.getDevice(id);
        if(device != null) {
            device.setPaired(false);
            EventBus.getDefault().post(new ShareModuleCommand(ModuleCmds.endWork,id));
        }
        dbHelper.deletePaired(id);
        EventBus.getDefault().post(new MainActivityCommand(UiCmds.UPDATE_ACTIVITY,null));
    }

    private void setDevicePair(String id,String data){
        Device device = BackgroundUtil.getDevice(id);
        if(device != null){
            device.setPaired(true);
        }
        dbHelper.savePairedDevice(id, data);
        EventBus.getDefault().post(new MainActivityCommand(UiCmds.UPDATE_ACTIVITY,null));
//        EventBus.getDefault().post(new ShareModuleCommand(ModuleCmds.sendSetupServerCommand,id));
    }

    public void setPairStatus(String id, String data, String result){
        if (result.equalsIgnoreCase(TcpConnectionManager.api.OK.name())) {
            setDevicePair(id,data);
        }else{
            unPairDevice(id);
        }
    }

    public void receivePairRequest(NetworkPackage networkPackage,Context context){
        guiCommunicator.requestPair(networkPackage,context);
    }

    public void receivePairSignal(NetworkPackage np,Context context){
        String id = np.getId();
        TcpDto dto = (TcpDto) np.getData();
        sendPairAnswer(id,dto.getResult(),context);
        setPairStatus(id,dto.getData(),dto.getResult());
    }

    public void pairRequestAnswerFromGui(String id, boolean answer, String data,Context context) {
        String result = answer ? TcpConnectionManager.api.OK.name() : TcpConnectionManager.api.REFUSE.name();
        sendPairAnswer(id,result,context);
        setPairStatus(id,data,result);
    }

    public void sendPairAnswer(String id, String answer,Context context)  {

        String data = String.valueOf(BackgroundUtil.getMyId().hashCode());
        TcpDto dto = new TcpDto(TcpConnectionManager.api.PAIR_RESULT.name(), data, answer);
        String message = messageFactory.createMessage(TcpConnectionManager.api.TCP.name(), false, dto);
        BackgroundUtil.sendToDevice(id, message, context);
    }
}
