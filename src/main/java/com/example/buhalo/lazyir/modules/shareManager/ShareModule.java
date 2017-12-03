package com.example.buhalo.lazyir.modules.shareManager;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.ContextCompat.getExternalFilesDirs;
import static com.example.buhalo.lazyir.modules.shareManager.ftpServer.ftpServerOn;

/**
 * Created by buhalo on 05.03.17.
 */
public class ShareModule extends Module {

    public static final String SHARE_T = "ShareModule";
    @Deprecated
    public static final String SHARE_TYPE = "share_type";
    public static final String SETUP_SERVER_AND_SEND_ME_PORT = "setup server and send me port";
    public static final String CONNECT_TO_ME_AND_RECEIVE_FILES = "connect to me and receive files"; // first arg port,second number of files - others files
    public static final String PORT = "port";




    private static SftpServer sftpServer;
    private static ftpServer ftpServer;
    private static boolean sftServerOn = false;
    private static int port = 0;
    private static int portFtp = 0;

    private boolean waitResponse;

    private List<FileWrap> responseList;
    private String rootPathFromServer;
    private String lastRootPathFromServer;

    @Override
    public void execute(NetworkPackage np) {

        if(np.getData().equals(SETUP_SERVER_AND_SEND_ME_PORT)) {
            if(np.getValue("os").equals("nix"))
            setupSftp(np,context.getApplicationContext());
            else if(np.getValue("os").equals("win"))
                setupSftp(np,context.getApplicationContext()); // now win too use's sftp
        }
    }

    @Override
    public void endWork() {

    }

    private void setupftp(NetworkPackage np, Context context) {
        if(ftpServer == null) {
            ftpServer = new ftpServer();
        }
        NetworkPackage pack = NetworkPackage.Cacher.getOrCreatePackage(SHARE_T,CONNECT_TO_ME_AND_RECEIVE_FILES);
        portFtp = ftpServer.setupFtpServer(context,np);
        if(portFtp == 0)
            portFtp = 9000;
        pack.setValue(PORT,Integer.toString(portFtp));
        String userName = ftpServer.getUser(np.getId());
        pack.setValue("userName",userName);
        pack.setValue("pass",ftpServer.getPass(userName));
        BackgroundService.sendToDevice(np.getId(),pack.getMessage());
    }

    private   void setupSftp(NetworkPackage np, Context context) {
        if (sftpServer == null) {
            sftpServer = new SftpServer();
        }
        NetworkPackage pack = NetworkPackage.Cacher.getOrCreatePackage(SHARE_T,CONNECT_TO_ME_AND_RECEIVE_FILES);
        if(!sftServerOn) {
            port = sftpServer.setupSftpServer(context);
            sftServerOn = true;
        }
        if(port == 0)
            port = 9000;
        pack.setValue(PORT,Integer.toString(port));
        pack.setValue("userName",SftpServer.USER);
        pack.setValue("pass",sftpServer.pass);
        pack.setValue("mainDir",BackgroundService.getAppContext().getFilesDir().getAbsolutePath()); // todo test
        pack.setObject("externalPath",new PathWrapper(getExternalStorageDirectories())); // todo in server
        sendMsg(pack.getMessage());
    }
    public static void stopSftpServer() {
        if(sftpServer!= null) {
            sftpServer.stopSftpServer();
        }
        sftServerOn = false;
        if(ftpServer != null) {
            ftpServer.stopFtp();
        }
        ftpServerOn = false;
    }

    /* returns external storage paths (directory of external memory card) as array of Strings */
    // https://stackoverflow.com/questions/36766016/how-to-get-sd-card-path-in-android6-0-programmatically
    public String[] getExternalStorageDirectories() {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = BackgroundService.getAppContext().getExternalFilesDirs(null);

            for (File file : externalDirs) {
                String path = file.getPath().split("/Android")[0];

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            StringBuilder output = new StringBuilder();
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output.append(new String(buffer));
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.toString().trim().isEmpty()) {
                String devicePoints[] = output.toString().split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d("ShareModule", results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d("ShareModule", results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }



    public String getRootPath()
    {
        return Environment.getExternalStorageDirectory().toString();
    }

 }

