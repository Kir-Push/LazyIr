package com.example.buhalo.lazyir.modules.share;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.ModuleCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.service.settings.SettingService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import lombok.Synchronized;

import static com.example.buhalo.lazyir.modules.share.ShareModule.api.CONNECT_TO_ME_AND_RECEIVE_FILES;
import static com.example.buhalo.lazyir.modules.share.ShareModule.api.RECCONECT;
import static com.example.buhalo.lazyir.modules.share.ShareModule.api.SETUP_SERVER_AND_SEND_ME_PORT;


public class ShareModule extends Module {
    private static final String TAG = "ShareModule";
    public enum api{
        SETUP_SERVER_AND_SEND_ME_PORT,
        CONNECT_TO_ME_AND_RECEIVE_FILES,
        RECCONECT
    }

    private static SftpServer sftpServer;
    private static boolean sftServerOn;
    private static int port = 0;
    private SettingService settingService;

    @Inject
    public ShareModule(MessageFactory messageFactory, Context context, SettingService settingService) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
        this.settingService = settingService;
    }

    @Override
    public void execute(NetworkPackage np) {
        ShareModuleDto dto = (ShareModuleDto) np.getData();
        String command = dto.getCommand();
        if(command.equals(SETUP_SERVER_AND_SEND_ME_PORT.name())){
           if( dto.getOsType().equals("nix") || dto.getOsType().equals("win")){
               setupSftp(messageFactory,settingService,device.getId(),context);
           }
        }else if(command.equals(RECCONECT.name())){
            sendReconnect();
        }
    }

    private void sendReconnect() {
        if(sftpServer != null) {
            ShareModuleDto shareModuleDto = new ShareModuleDto(RECCONECT.name());
            shareModuleDto.setPort(port);
            String user = sftpServer.getUser();
            String pass = sftpServer.getPass();
            if(user == null || pass == null){
                Log.e(TAG,"Something goes wrong, sendRecconect user: " + user + " pass: " + pass);
                return;
            }
            shareModuleDto.setUserName(user);
            shareModuleDto.setPassword(pass);
            shareModuleDto.setMountPoint(Environment.getExternalStorageDirectory().getAbsolutePath());
            shareModuleDto.setExternalMountPoint(new PathWrapper(Arrays.asList(getExternalStorageDirectories(context))));
            String message = messageFactory.createMessage(ShareModule.class.getSimpleName(), true, shareModuleDto);
            sendMsg(message);
        }
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
        if(BackgroundUtil.ifLastConnectedDeviceAreYou(device.getId())) {
            stopSftpServer();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void receiveCommand(ShareModuleCommand cmd){
        if(cmd.getId().equals(device.getId()) && cmd.getCommand().equals(ModuleCmds.endWork)){
            endWork();
        }
        else if (cmd.getId().equals(device.getId()) && cmd.getCommand().equals(ModuleCmds.sendSetupServerCommand)) {
            setupSftp(messageFactory,settingService,device.getId(),context);
        }
    }

    @Synchronized
    private static void setupSftp(MessageFactory messageFactory,SettingService settingService,String id, Context context) {
        if (sftpServer == null) {
            sftpServer = new SftpServer();
        }
        if(!sftServerOn) {
            port = sftpServer.setupSftpServer(settingService,context);
            sftServerOn = true;
        }
        if(port == 0) {
            port = 9000;
        }
        ShareModuleDto shareModuleDto = new ShareModuleDto(CONNECT_TO_ME_AND_RECEIVE_FILES.name());
        shareModuleDto.setPort(port);
        shareModuleDto.setUserName(sftpServer.getUser());
        shareModuleDto.setPassword(sftpServer.getPass());
        shareModuleDto.setMountPoint(Environment.getExternalStorageDirectory().getAbsolutePath());
        shareModuleDto.setExternalMountPoint(new PathWrapper(Arrays.asList(getExternalStorageDirectories(context))));
        String message = messageFactory.createMessage(ShareModule.class.getSimpleName(), true, shareModuleDto);
        BackgroundUtil.sendToDevice(id,message,context);
    }

    @Synchronized
    private static void stopSftpServer() {
        if(sftpServer!= null) {
            sftpServer.stopSftpServer();
        }
        sftServerOn = false;
    }

    /* returns external storage paths (directory of external memory card) as array of Strings */
    // https://stackoverflow.com/questions/36766016/how-to-get-sd-card-path-in-android6-0-programmatically
    private static String[] getExternalStorageDirectories(Context context) {
        List<String> results = getExternalForKitkat(context);
        if (results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            getExternalForOtherVersions(results);
        }
        //Below few lines is to remove paths which may not be external memory card, like OTG
        int i = 0;
        while (i < results.size()) {
            if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                results.remove(i);
            } else {
                i++;
            }
        }
        return results.toArray(new String[results.size()]);
    }

    private static void getExternalForOtherVersions(List<String> results) {

        StringBuilder output = new StringBuilder();
        try {
            final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold").redirectErrorStream(true).start();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                output.append(new String(buffer));
            }
            is.close();
        } catch (IOException e) {
            Log.e(TAG,"error in getExternalStorageDirectories",e);
        }
        if(!output.toString().trim().isEmpty()) {
            String[] devicePoints = output.toString().split("\n");
            for(String voldPoint: devicePoints) {
                results.add(voldPoint.split(" ")[2]);
            }
        }
    }

    private static List<String> getExternalForKitkat(Context context) {
        List<String> results = new ArrayList<>();
        File[] externalDirs = context.getExternalFilesDirs(null);
        for (File file : externalDirs) {
            String path = file.getPath().split("/Android")[0];
            if(Environment.isExternalStorageRemovable(file)){
                results.add(path);
            }
        }
        return results;
    }

 }

