package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.buhalo.lazyir.service.WifiListener.checkWifiOnAndConnected;

/**
 * Created by buhalo on 19.02.17.
 */

public class UdpBroadcastManager  {

    public static final String BROADCAST_INTRODUCE = "broadcast introduce";
    public static final String BROADCAST_INTRODUCE_MSG = "I search Adventures";
    private static DatagramSocket socket;
    private static volatile DatagramSocket server;
    private static volatile int send_period = 15000;
    private int  count = 0;


    private volatile static boolean listening = false;
    private volatile static boolean exitedFromSend = true;
    private volatile static boolean sending;
    private static Lock lock = new ReentrantLock();
    private static UdpBroadcastManager instance;
    private  ScheduledFuture<?> sendingFuture = null;

    private UdpBroadcastManager() {
        try {
            configureManager();
        } catch (IOException e) {
            Log.e("Udp","Error in udp configure method");
        }
    }

    private DatagramSocket configureManager() throws IOException {
        socket = new DatagramSocket();
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
        return socket;
    }


    public static UdpBroadcastManager getInstance(){
        if(instance == null)
            instance = new UdpBroadcastManager();
        return instance;
    }


    private void sendBroadcast(final String message, final int port)
    {
        lock.lock();
        if(socket == null){
            BackgroundService.addCommandToQueue(BackgroundServiceCmds.stopUdpListener);
            return;
        }
                try {
                    InetAddress broadcastAddress = getBroadcastAddress(BackgroundService.getAppContext());
                    byte[] sendData = message.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, port);
                    Log.d("Udp","Sending broadcast: "+ message);
                    socket.send(sendPacket);
                } catch (IOException e) {
                    Log.e("Udp",e.toString() + " " + Thread.currentThread(),e);
                }finally {
            lock.unlock();
                }

    }

    private InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi != null ? wifi.getDhcpInfo() : null;
        // handle null somehow
        if(dhcp == null)
            return InetAddress.getByName("255.255.255.255");

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

    void startUdpListener(final Context context,int port)
    {
        lock.lock();
        try {
        if(isListening() || server != null) {
            stopUdpListener();
            Log.d("Udp","listening already working");
        }
            if (!checkWifiOnAndConnected(context)) return;


                server = new DatagramSocket(port);
                server.setReuseAddress(true);
                setListening(true);
                BackgroundService.submitNewTask(()->{
                    Log.d("Udp","start listening");
                    final int bufferSize = 1024 * 5;
                    byte[] data = new byte[bufferSize];
                    while (isListening()) {
                        if(server == null)
                            break;
                        DatagramPacket packet = new DatagramPacket(data, bufferSize);
                        try {
                            server.receive(packet);
                        } catch (Exception e) {
                            Log.e("Udp", "UdpReceive exception",e);
                            break;
                        }
                        if(isListening())
                        udpReceived(packet,context);
                        data = new byte[bufferSize];
                    }
                    Log.d("Udp", "Stopping UDP listener");
//                    BackgroundService.addCommandToQueue(BackgroundServiceCmds.stopUdpListener);
                });
            } catch (Throwable e) {
                Log.e("Udp","Error creating server socket",e);
            }finally {
                lock.unlock();
            }
    }



    private void udpReceived(DatagramPacket packet, Context context)
    {
        String pck = new String(packet.getData(),packet.getOffset(),packet.getLength());
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(pck);
        System.out.println("Received Udp " + pck);
        if(np.getId().equals(NetworkPackage.getMyId()))
        { // my own broadcast, ignore it
        }
        else if(np.getType().equals(BROADCAST_INTRODUCE))
            if(!TcpConnectionManager.getInstance().checkExistingConnection(np.getId())) {
                TcpConnectionManager.getInstance().receivedUdpIntroduce(packet.getAddress(), BackgroundService.getPort(), np,context);
            }
    }


    void stopUdpListener()
    {
        setListening(false);
        lock.lock();
        try{
        if(server != null)
        server.close();
        server = null;}
        finally {
            lock.unlock();
        }
    }


    void startSendingTask(final Context context, final int port) {
        if (!checkWifiOnAndConnected(context)) return;
        lock.lock();
        try {
            if(sendingFuture != null || isSending())
                stopSending();
        startSending();
        count = 0;
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(BROADCAST_INTRODUCE,BROADCAST_INTRODUCE_MSG);

            final String message  = np.getMessage();
            if(socket == null) {
                socket = configureManager();
            }
            sendingFuture = BackgroundService.getTimerService().scheduleWithFixedDelay(() -> {
                if(!isSending())
                    return;
                    sendBroadcast(message, port);
                        count++;
                        if (count == 20 && send_period < 30000) {
                            setSend_period(send_period * 2);
                            updateSender();
                        } else if (count == 40 && send_period <= 30000) {
                            setSend_period(send_period * 2);
                            updateSender();
                        }
            }, 0, getSend_period(), TimeUnit.MILLISECONDS);
        }
        catch (IOException e) {
            Log.e("Udp","Error in StartSendingTask",e);
            stopSending();
        }finally {
            lock.unlock();
        }
    }

    static boolean isSending() {return sending;}

    void startSending() {sending = true;}

    private void updateSender(){
        lock.lock();
        try {
            BackgroundService.addCommandToQueue(BackgroundServiceCmds.stopSendingPeriodicallyUdp);
            BackgroundService.addCommandToQueue(BackgroundServiceCmds.startSendPeriodicallyUdp);
        }finally {
            lock.unlock();
        }
    }

    void stopSending() {
        sending = false;
            if (sendingFuture != null)
                sendingFuture.cancel(true);
            sendingFuture = null;
    }

    int getSend_period() {
        return send_period;
    }

    void setSend_period(int send_period) {
        lock.lock();
        this.send_period = send_period;
        lock.unlock();
    }

    void onZeroConnections() {
        stopSending();
        setSend_period(15000);
        updateSender();
       count = 0;
    }

    public static boolean isListening() {
        return listening;
    }

    public static void setListening(boolean listening) {
        UdpBroadcastManager.listening = listening;
    }
}
