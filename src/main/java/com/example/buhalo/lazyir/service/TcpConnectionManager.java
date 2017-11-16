package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.service.network.tcp.ConnectionThread;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static com.example.buhalo.lazyir.Devices.NetworkPackage.DEVICE_TYPE;
import static com.example.buhalo.lazyir.service.BackgroundService.port;

/**
 * Created by buhalo on 19.02.17.
 */

public class TcpConnectionManager {
    public final static String TCP_INTRODUCE = "tcpIntroduce";
    public final static String TCP_PING = "ping pong";
    public final static String TCP_PAIR_RESULT = "pairedresult";
    public final static String RESULT = "result";
    public final static String OK = "ok";
    public final static String REFUSE = "refuse";
    public final static String TCP_PAIR = "pair";
    public final static String TCP_UNPAIR = "unpair";
    public final static String TCP_SYNC = "sync";
    public static TcpConnectionManager instance;

    private static volatile boolean ServerOn = false;
    ServerSocket myServerSocket;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private TcpConnectionManager() {
    }


    static TcpConnectionManager getInstance()
    {
        if(instance == null)
            instance = new TcpConnectionManager();
        return instance;
    }


    public void startListening(final int port, final Context context) {
       BackgroundService.executorService.submit(new Runnable() {
           @Override
           public void run() {
               if(ServerOn) {
                   Log.d("Tcp","Server already working");
                   return;
               }
               if(myServerSocket == null || myServerSocket.isClosed()) {
                   try {
                       myServerSocket = new ServerSocket(port);
                   } catch (IOException e) {
                       Log.e("Tcp",e.toString());
                   }
               }
               ServerOn = true;
               while(ServerOn) {
                   Socket socket;
                   try {
                       socket = myServerSocket.accept();
                       socket.setKeepAlive(true);
                       BackgroundService.executorService.submit(new ConnectionThread(socket, context));
                   } catch (IOException e) {
                       Log.e("Tpc","Exception on accept connection ignoring + ",e);
                       if(myServerSocket.isClosed())
                           ServerOn = false;
                   }
               }
               try {
                   myServerSocket.close();
                   Log.d("Tcp","Closing server");
               }catch (IOException e) {
                   Log.e("Tcp","error in closing connecton");
               }
           }
       });
    }

    public void stopListening() {
        ServerOn = false;
        try {
            if(myServerSocket != null)
            myServerSocket.close();
        } catch (IOException e) {
            Log.e("Tcp","Error in close serverSocket ",e);
        }
    }

    // configure sslsocket for tls connection
    protected Socket getConnection(InetAddress ip, int port,Context context) throws IOException  {
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.testkeys);
            trustStore.load(trustStoreStream, "bimkaSamokat".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLSocketFactory factory = sslContext.getSocketFactory();
            return factory.createSocket(ip, port);
        } catch (GeneralSecurityException e) {
            Log.e(this.getClass().toString(), "Exception while creating context: ", e);
            throw new IOException("Could not connect to SSL Server", e);
        }
    }





    public void receivedUdpIntroduce(InetAddress address, int port,NetworkPackage np, Context context) {
        try {
            // at this moment connect only to pc
            if(!np.getValue(DEVICE_TYPE).equals("pc"))
                return;
            Socket socket = getConnection(address,port,context);
            // submit connection to executorService - this service is main for app
            BackgroundService.executorService.submit(new ConnectionThread(socket, context));
        } catch (Exception e) {
            Log.e("Tcp","Exception on accept connection ignoring +",e);
        }
    }

    public void sendCommandToAll(String message) {
        if(Device.getConnectedDevices().size() == 0) {
            return;
        }
        for (Device device : Device.getConnectedDevices().values()) {
            sendCommandToServer(device.getId(),message);
        }

    }

//todo add  checking wheter module enabled or not, and everything about it

    public void sendCommandToServerOLd(final String id, final String command) {
        lock.writeLock().lock();
        try {
            Device device = Device.connectedDevices.get(id);
            if (device == null || !device.isConnected()) {
                Log.d("Tcp", "Error in output for jasechsocket");
                stopListening(id);
                return;
            }
            if (!device.isPaired()) {
                Log.d("Tcp", "Device is not paired and so on not allowed to continue");
                return;
            }
            device.printToOut(command);
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void sendPairing(String id)
    {
        NetworkPackage networkPackage = NetworkPackage.Cacher.getOrCreatePackage(TCP_PAIR,String.valueOf(android.os.Build.SERIAL.hashCode()));
        sendCommandToServer(id,networkPackage.getMessage());
    }

    //send unpair command to server
    // remove pair note from db
    public void unpair(String id,Context context) {
        DBHelper.getInstance(context).deletePaired(id);
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(TCP_UNPAIR, TCP_UNPAIR);
        sendCommandToServer(id,orCreatePackage.getMessage());
        Device.getConnectedDevices().get(id).setPaired(false);
        //   unPairAction(id); //todo
    }




    public void stopListening(Device closingDevice) {
        if(closingDevice == null)
            return;
        closingDevice.closeConnection();
    }


    public boolean checkExistingConnection(String dvId) {
       if(!Device.getConnectedDevices().containsKey(dvId)) {
           return false;
       }
        if(Device.getConnectedDevices().get(dvId).getSocket() == null || !Device.getConnectedDevices().get(dvId).getSocket().isConnected()) {
            stopListening(dvId);
            return false;
        }return true;
    }
}
