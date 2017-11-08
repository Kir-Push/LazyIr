package com.example.buhalo.lazyir.service.network.tcp;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.battery.Battery;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
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
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_PING;
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

    ConnectionThread(Socket socket,Context context) throws SocketException {
        this.connection = socket;
        this.context = context;
        // enabling keepAlive and timeout to close socket,
        // Android has some battery safe methods and act not fully predictable, you don't know,
        // when he run netowork code in background, so you need wait more.
        connection.setKeepAlive(true);
        connection.setSoTimeout(60000);
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
    public void requestPairFromUser(NetworkPackage np)
    {
        BackgroundService.getTcp().reguestPair(np);
    }

    // printing method,
    // locked for concurrency error's avoiding
    public void printToOut(String message)
    {
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

    @Override
    public void run() {

    }


    public void pairResult(NetworkPackage np)
    {
        if(np.getData() != null &&  deviceId != null)
        {
            if(np.getValue(RESULT).equals(OK))
            {
                DBHelper.getInstance(context).savePairedDevice(deviceId,np.getData());
                Device.getConnectedDevices().get(deviceId).setPaired(true);
                Battery.sendBatteryLevel(deviceId,context); // send battery level first after pairing, or maybe after sync?
            }
            else if(np.getValue(RESULT).equals(REFUSE))
            {
                DBHelper.getInstance(context).deletePaired(deviceId);
                Device.getConnectedDevices().get(deviceId).setPaired(false);
            }
        }
        else {
            Device.getConnectedDevices().get(deviceId).setPaired(false); // todo check for nullPointer
        }
    }
}
