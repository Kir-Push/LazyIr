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
    private static TcpConnectionManager instance;

    private TcpConnectionManager() {}


    static TcpConnectionManager getInstance() {
        if(instance == null)
            instance = new TcpConnectionManager();
        return instance;
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





     void receivedUdpIntroduce(InetAddress address, int port,NetworkPackage np, Context context) {
        try {
            // at this moment connect only to pc
            if(!np.getValue(DEVICE_TYPE).equals("pc"))
                return;
            Socket socket = getConnection(address,port,context);
            // submit connection to executorService - this service is main for app
            BackgroundService.submitNewTask(new ConnectionThread(socket, context));
        } catch (IOException e) {
            Log.e("Tcp","Exception on accept connection ignoring ",e);
        }
    }

    public void sendCommandToAll(String message) {BackgroundService.sendToAllDevices(message);}


    public static void sendPairing(String id) {
        NetworkPackage networkPackage = NetworkPackage.Cacher.getOrCreatePackage(TCP_PAIR,String.valueOf(NetworkPackage.getMyId().hashCode()));
        BackgroundService.sendToDevice(id,networkPackage.getMessage());
    }




    void stopListening(Device closingDevice) {
        if(closingDevice == null)
            return;
        closingDevice.closeConnection();
    }


    boolean checkExistingConnection(String dvId) {
       if(!Device.getConnectedDevices().containsKey(dvId)) {
           return false;
       }
        Device device = Device.getConnectedDevices().get(dvId);
        if(device.getSocket() == null || !device.getSocket().isConnected()) {
            stopListening(device);
            return false;
        }return true;
    }
}
