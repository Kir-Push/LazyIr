package com.example.buhalo.lazyir.service.network.tcp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.CommandsList;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.ModuleSetting;
import com.example.buhalo.lazyir.Devices.ModuleSettingList;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.battery.Battery;
import com.example.buhalo.lazyir.modules.reminder.Reminder;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.NotifActionReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLSocket;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.buhalo.lazyir.Devices.NetworkPackage.N_OBJECT;
import static com.example.buhalo.lazyir.service.BackgroundServiceCmds.onZeroConnections;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.OK;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.REFUSE;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.RESULT;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_INTRODUCE;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_PAIR;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_PAIR_RESULT;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_PING;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_SYNC;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_UNPAIR;

/**
 * Created by buhalo on 08.11.17.
 */

// class for connection work, it represent device connection, and pass messages to modules if needed.
public class ConnectionThread implements Runnable {
    private volatile Socket connection;


    private Context context;
    private String deviceId = null;
    private volatile boolean connectionRun;
    private volatile BufferedReader in = null;
    private volatile PrintWriter out = null;
    private ScheduledFuture<?> timerFuture;
    private Lock lock = new ReentrantLock();

    public ConnectionThread(Socket socket,Context context) throws SocketException {
        this.connection = socket;
        this.context = context;
        // enabling keepAlive and timeout to close socket,
        // Android has some battery safe methods and act not fully predictable, you don't know,
        // when he run netowork code in background, so you need wait more.
        connection.setKeepAlive(true);
        connection.setSoTimeout(60000);
    }

    @Override
    public void run() {
        try{
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
            connectionRun = true;
            while (connectionRun)
            {
                String clientCommand = in.readLine();
                if(clientCommand == null) {
                    connectionRun = false;
                    continue;
                }
                NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(clientCommand);
                determineWhatTodo(np);
            }
        }catch (IOException e){
            connectionRun = false;
            Log.e("Tcp","Error in tcp out",e);
        }finally {
            closeConnection();
        }
    }

    // it's method does not used now, but it's using in server side, and in future
    // if will be needed to use android as server, it will may be useful
    private void configureSSLSocket() throws IOException
    {
        if(connection != null && connection instanceof SSLSocket) {
            ((SSLSocket)connection).setEnabledCipherSuites(((SSLSocket)connection).getSupportedCipherSuites());
            ((SSLSocket)connection).startHandshake();
        }
    }


    // get myId and send introduce package to device
    private void sendIntroduce(boolean paired) {
            String temp =String.valueOf(android.os.Build.SERIAL.hashCode());
            if(!paired)
                temp = "nonPaired";
            NetworkPackage networkPackage =  NetworkPackage.Cacher.getOrCreatePackage(TCP_INTRODUCE,temp);
            ModuleSettingList moduleSettingList = new ModuleSettingList();
        List<ModuleSetting> myEnabledModulesToModuleSetting = BackgroundService.getMyEnabledModulesToModuleSetting();
        moduleSettingList.setModuleSettingList(myEnabledModulesToModuleSetting);
            networkPackage.setObject(N_OBJECT,moduleSettingList);  // set myModuleConfig's to introduce package, android do the same
            printToOut(networkPackage.getMessage());
    }

    // ask user for pairing in notification
    // todo create notification which ask user for pairing, and everything needed for it.
    public void requestPairFromUser(NetworkPackage np) {
      //  BackgroundServiceOld.getTcp().reguestPair(np);
    }

    // printing method,
    // it's print to server over tcp connection
    // locked to avoiding concurrency error's
    public void printToOut(String message) {
        lock.lock();
        try{
            if(out == null) {
                return;
            }
            out.println(message);
            out.flush();
        }finally {
            lock.unlock();
        }
    }



 // pair result from device
    public void pairResult(NetworkPackage np) {
        Device device = Device.getConnectedDevices().get(deviceId);
        if(np.getData() != null &&  deviceId != null) {
            if(np.getValue(RESULT).equals(OK)) {
                DBHelper.getInstance(context).savePairedDevice(deviceId,np.getData());
                if(device != null)
                device.setPaired(true);
                Battery.sendBatteryLevel(deviceId,context); // send battery level first after pairing, or maybe after sync?
            }
            else if(np.getValue(RESULT).equals(REFUSE)) {
                DBHelper.getInstance(context).deletePaired(deviceId);
                if(device != null)
                device.setPaired(false);
            }
        } else {
            if(device != null)
            device.setPaired(false);
        }
        MainActivity.updateActivity();
    }


    // send ping to device
    private void ping() {
        printToOut(NetworkPackage.Cacher.getOrCreatePackage(TCP_PING,TCP_PING).getMessage());
    }


    // instanciate new device, put it in map and check in db if it paired.
    // after that send introduce package
    private void newConnectedDevice(NetworkPackage np)
    {
        if(deviceId == null) {
            deviceId = np.getId();
            boolean paired = true;
            ModuleSettingList object = np.getObject(N_OBJECT, ModuleSettingList.class);
            Device device = new Device(connection, deviceId, np.getName(), connection.getInetAddress(), np.getValue(NetworkPackage.DEVICE_TYPE), this,  object.getModuleSettingList(),context);
            Device.getConnectedDevices().put(deviceId, device);
            Reminder.startReminderTasks();
            if(MainActivity.getSelected_id().equals("")) {
                MainActivity.setSelected_id(deviceId);
            }
            if(np.getData() != null) {
                String pairedCode = np.getData();
                List<String> savedPairedCode = DBHelper.getInstance(context).getPairedCode(deviceId);
                if(savedPairedCode.size() != 0) {
                    if( savedPairedCode.get(0).equals(pairedCode)) {
                        device.setPaired(true);
                        paired = true;
                    }else{
                        device.setPaired(false);
                        paired = false;
                    }
                }
            }
            sendIntroduce(paired);
            //  startPingPong(deviceId);
        }
    }




    // closeConnection method.
    // first check future and cansel if not null,
    // after that iterate over all modules and disable them(they itself handle,if they will work after disabling)
    // finally remove device from list of connected devices,close socket's and out, in.
    public void closeConnection() {
        lock.lock();
        try {
            if (timerFuture != null && !timerFuture.isDone()) {
                timerFuture.cancel(true);
            }
            Device device = Device.getConnectedDevices().get(deviceId);
            if(device != null)
            for (Module module : device.getEnabledModules().values()) {
                if(module != null)
                    module.endWork();
            }
            Device.getConnectedDevices().remove(deviceId);
            // calling after because can throw exception and remove from hashmap won't be done
            in.close();
            out.close();
            connection.close();
        }catch (Exception e) {Log.e("ConnectionThread","Error in stopped connection",e);}
        finally {
            // call in finally becaus it may be called in end (after remove device from list)
            // but error can be throwed before it.
            BackgroundService.addCommandToQueue(onZeroConnections);
            Log.d("ConnectionThread", deviceId + " - Stopped connection");
            lock.unlock();
        }
    }

    //check if connected
    public boolean isConnected() {
        lock.lock();
        try {
            return connection != null && out != null && in != null && connectionRun;
        }finally {
            lock.unlock();
        }
    }

    // this method does not used at this time, maybe in future, but you will need rewrite it
    private void receiveSync(NetworkPackage np) {
        try {
            List<Command> receivedCommands = np.getObject(N_OBJECT, CommandsList.class).getCommands();
            if(receivedCommands == null)
                return;
            //  DBHelper.getInstance(context).removeCommandsPcAll();
            List<String> commandsPc = DBHelper.getInstance(context).getCommandsPc();
            HashSet<String> pcSet = new HashSet<>(commandsPc);
            for (Command receivedCommand : receivedCommands) {
                if(!pcSet.contains(receivedCommand.getCommand_name()))
                    DBHelper.getInstance(context).saveCommand(receivedCommand);
            }
        }catch (Exception e) {
            Log.e("Tcp",e.toString());
        }
    }

    // work with command from client, get name of module, and pass package to it
    public void commandFromClient(NetworkPackage np) {
        try {
            Device device = Device.getConnectedDevices().get(np.getId());
            if(device == null)
                return;
            if(!device.isPaired()) {
                return;
            }
            String moduleType = np.getType();
            boolean myModuleSetting = BackgroundService.getMyEnabledModules().contains(moduleType);
            if(!myModuleSetting){
                return;
            }
            Module module = device.getEnabledModules().get(moduleType);
            if(module != null)
            module.execute(np);
        }catch (Exception e) {
            Log.e("Tcp",e.toString());
        }
    }


    // determine what to do with package
    // it depends of package's type value
    public void determineWhatTodo(NetworkPackage np) {
        switch (np.getType()) {
            case TCP_INTRODUCE:
                newConnectedDevice(np);
                break;
            case TCP_PING:
                Device.getConnectedDevices().get(deviceId).setAnswer(true);
                ping();
                break;
            case TCP_PAIR_RESULT:
                pairResult(np);
                break;
            case TCP_SYNC:
                receiveSync(np);
                break;
            case TCP_UNPAIR:
                unpair(np);
                break;
            case TCP_PAIR:
                receiveRequestPair(np);
                break;
            default:
                Device.getConnectedDevices().get(deviceId).setAnswer(true);
                commandFromClient(np);
                break;
        }
    }

    private void unpair(NetworkPackage np) {
        String id = np.getId();
        Device device = Device.getConnectedDevices().get(id);
        DBHelper.getInstance(context).deletePaired(deviceId);
        if(device != null)
            device.setPaired(false);
        MainActivity.updateActivity();
    }

    private void receiveRequestPair(NetworkPackage np) {
        String id = "775533";
        String data = np.getData();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context,id)
                        .setSmallIcon(R.mipmap.no_pair)
                        .setContentTitle(np.getName() + " Request pairing!")
                        .setContentText("You want to be friend?");
        Intent yesAction = new Intent(context,NotifActionReceiver.class);
        yesAction.setAction("Yes");
        yesAction.putExtra("id",np.getId());
        yesAction.putExtra("value",data);
        Intent noReceive = new Intent(context,NotifActionReceiver.class);
        noReceive.setAction("No");
        noReceive.putExtra("id",np.getId());
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(BackgroundService.getAppContext(), 775533, yesAction, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(BackgroundService.getAppContext(), 775533, noReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.mipmap.yes_pair,"Yes",pendingIntentYes);
        mBuilder.addAction(R.drawable.delete48,"No",pendingIntentNo);
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(775533, mBuilder.build());
    }

    public Context getContext() {
        return context;
    }


    //todo check security issue, when someonew send message with other id, check whetrher ID equal to ID in connectionThread
}
