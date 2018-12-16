package com.example.buhalo.lazyir.service.network.udp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.service.network.tcp.TcpConnectionManager;
import com.example.buhalo.lazyir.service.settings.SettingService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

import static com.example.buhalo.lazyir.service.BackgroundUtil.checkWifiOnAndConnected;
import static com.example.buhalo.lazyir.service.BackgroundUtil.hasActualConnection;
import static com.example.buhalo.lazyir.service.network.udp.UdpBroadcastManager.api.BROADCAST_INTRODUCE;

public class UdpBroadcastManager  {
    private static final String TAG = "UdpBroadcastManager";

    public enum api{
        BROADCAST_INTRODUCE,
        INTRODUCE
    }
    private DatagramSocket socket;
    private DatagramSocket server;
    private TcpConnectionManager tcp;
    private MessageFactory messageFactory;
    private SettingService settingService;
    private Context context;

    private boolean listening;
    private boolean sending;
    private ScheduledFuture<?> sendingFuture;
    @Getter @Setter
    private static HashSet<String> connectedUdp = new HashSet<>();


    @Inject
    public UdpBroadcastManager(TcpConnectionManager tcp,MessageFactory messageFactory,SettingService settingService,Context context) {
        this.tcp = tcp;
        this.context = context;
        this.messageFactory = messageFactory;
        this.settingService = settingService;
    }

    @Synchronized
    private DatagramSocket configureManager() {
        try {
            socket = new DatagramSocket();
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
        } catch (IOException e) {
            Log.e(TAG,"Error in udp configure method");
        }
        return socket;
    }

    @Synchronized
    private void initServerSocket(int port){
        try {
            if (!checkWifiOnAndConnected(context)) {
                return;
            }
            server = new DatagramSocket(port);
            server.setReuseAddress(true);
            setListening(true);
        }catch (SocketException e){
            Log.e(TAG,"Error creating server socket",e);
        }
    }

    @Synchronized
    private void sendBroadcast(final String message, final int port) {
        if (socket == null) {
            configureManager();
        }
        try {
            InetAddress broadcastAddress = getBroadcastAddress(context);
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, port);
            socket.send(sendPacket);
        } catch (IOException e) {
            Log.e(TAG, "error in sendBroadcast message: " + message + " port: " + port, e);
        }
    }

    private InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi != null ? wifi.getDhcpInfo() : null;
        // handle null somehow
        if(dhcp == null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

    public void startUdpListener(final Context context, int port) {
        BackgroundUtil.submitTask(() -> {
            if (isListening() || server != null) {
                Log.d("Udp", "listening already working");
            }
            initServerSocket(port);

            final int bufferSize = 1024 * 5;
            try {
                while (isListening()) {
                    if (server == null) {
                        return;
                    }
                    byte[] data = new byte[bufferSize];
                    DatagramPacket packet = new DatagramPacket(data, bufferSize);
                    server.receive(packet);
                    udpReceived(packet, context);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating server socket", e);
            }
            Log.d(TAG, "Stopping UDP listener");
        }, context);
    }



    private void udpReceived(DatagramPacket packet, Context context) {
        String pck = new String(packet.getData(),packet.getOffset(),packet.getLength());
        NetworkPackage networkPackage = messageFactory.parseMessage(pck);
        String id = networkPackage.getId();
        String type = networkPackage.getType();
        if(!id.equals(BackgroundUtil.getMyId()) && type.equals(api.INTRODUCE.name()) && !connectedUdp.contains(id)) {
            boolean connected = BackgroundUtil.checkExistingConnection(id);
            if(!connected){
                connectedUdp.add(id);
                tcp.receivedUdpIntroduce(packet.getAddress(),Integer.parseInt(settingService.getValue("TCP-port")),networkPackage,context);
            }
        }
    }


    @Synchronized
    public void stopUdpListener() {
        if(server != null) {
            server.close();
        }
        server = null;
        setListening(false);
    }


    @Synchronized
    public void startSendingTask(final Context context, final int port) {
        if (!checkWifiOnAndConnected(context)) {
            return;
        }
        if (sendingFuture != null || isSending()) {
            stopSending();
        }
        setSending(true);
        final String message = messageFactory.createMessage(BROADCAST_INTRODUCE.name(), false, null);
        if (socket == null) {
            socket = configureManager();
        }
        sendingFuture = BackgroundUtil.getTimerExecutor().scheduleWithFixedDelay(() -> {
            if (!isSending()) {
                return;
            }
            sendBroadcast(message, port);
        }, 0, getSendPeriod(), TimeUnit.MILLISECONDS);
    }

    private boolean isSending() {return sending;}

    private void setSending(boolean sending) {this.sending = sending;}


    @Synchronized
    private void stopSending() {
        if (sendingFuture != null) {
            sendingFuture.cancel(true);
        }
        sendingFuture = null;
        setSending(false);
    }

    @Synchronized
    private int getSendPeriod() {
        return 15000;
    }


    private boolean isListening() {
        return listening;
    }

    private void setListening(boolean listening) {
        this.listening = listening;
    }

    public void cacheConnection() {
        String message = messageFactory.createMessage(BROADCAST_INTRODUCE.name(), false, null);
        sendBroadcast(message, Integer.parseInt(settingService.getValue("TCP-port")));
    }
}
