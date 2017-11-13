package com.example.buhalo.lazyir.service.network.tcp;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.CommandsList;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.battery.Battery;
import com.example.buhalo.lazyir.modules.shareManager.ShareModule;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.UdpBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

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
    private void sendIntroduce() {
            String temp =String.valueOf(android.os.Build.SERIAL.hashCode());
            NetworkPackage networkPackage =  NetworkPackage.Cacher.getOrCreatePackage(TCP_INTRODUCE,temp);
            printToOut(networkPackage.getMessage());
    }

    // ask user for pairing in notification
    // todo create notification which ask user for pairing, and everything needed for it.
    public void requestPairFromUser(NetworkPackage np) {
      //  BackgroundService.getTcp().reguestPair(np);
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
        if(np.getData() != null &&  deviceId != null) {
            if(np.getValue(RESULT).equals(OK)) {
                DBHelper.getInstance(context).savePairedDevice(deviceId,np.getData());
                Device.getConnectedDevices().get(deviceId).setPaired(true);
                Battery.sendBatteryLevel(deviceId,context); // send battery level first after pairing, or maybe after sync?
            }
            else if(np.getValue(RESULT).equals(REFUSE)) {
                DBHelper.getInstance(context).deletePaired(deviceId);
                Device.getConnectedDevices().get(deviceId).setPaired(false);
            }
        } else {
            Device.getConnectedDevices().get(deviceId).setPaired(false); // todo check for nullPointer
        }
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
            Device device = new Device(connection, deviceId, np.getName(), connection.getInetAddress(), np.getValue(NetworkPackage.DEVICE_TYPE), this,context);
            Device.getConnectedDevices().put(deviceId, device);
            if(MainActivity.getSelected_id().equals("")) {
                MainActivity.setSelected_id(deviceId);
            }
            if(np.getData() != null) {
                String pairedCode = np.getData();
                List<String> savedPairedCode = DBHelper.getInstance(context).getPairedCode(deviceId);
                if(savedPairedCode.size() != 0 && savedPairedCode.get(0).equals(pairedCode)) {
                    device.setPaired(true);
                }
            }
            sendIntroduce();
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
            for (Module module : Device.getConnectedDevices().get(deviceId).getEnabledModules().values()) {
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
            BackgroundService.createIntentForBackground(context,BackgroundService.onZeroConnections);
            lock.unlock();
            Log.d("ConnectionThread", deviceId + " - Stopped connection");}
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
            List<Command> receivedCommands = np.getObject(NetworkPackage.N_OBJECT, CommandsList.class).getCommands();
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
            if(!device.isPaired()) {
                return;
            }
            Module module = device.getEnabledModules().get(np.getType());
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
                Device.connectedDevices.get(deviceId).setAnswer(true);
                ping();
                break;
            case TCP_PAIR_RESULT:
                pairResult(np);
                break;
            case TCP_SYNC:
                receiveSync(np);
                break;
            default:
                Device.connectedDevices.get(deviceId).setAnswer(true);
                commandFromClient(np);
                break;
        }
    }


    //todo check security issue, when someonew send message with other id, check whetrher ID equal to ID in connectionThread
}
