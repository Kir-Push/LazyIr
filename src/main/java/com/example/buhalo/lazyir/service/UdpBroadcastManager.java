package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.Exception.ParseError;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by buhalo on 19.02.17.
 */

public class UdpBroadcastManager  {

    private static final String BROADCAST_INTRODUCE = "broadcast introduce";
    private static final String BROADCAST_INTRODUCE_MSG = "I search Adventures";
    private DatagramSocket socket;
    private  DatagramSocket server;
    private InetAddress broadcastAddress;
    private String android_id;
    private String android_name;
    private int send_period = 15000;
    public int  count = 0;


    private volatile static boolean listening = false;
    public volatile static boolean exitedFromSend = true;
    private volatile static boolean sending;
    private static UdpBroadcastManager instance;

    public static HashMap<String,InetAddress> neighboors = new HashMap<>();

    private UdpBroadcastManager() {
        try {
            configureManager();
        } catch (IOException e) {
            Log.e("Udp","Error in udp configure method");
        }
    }

    private void configureManager() throws IOException
    {

        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        socket = new DatagramSocket();
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
    }


    public static UdpBroadcastManager getInstance()
    {
        if(instance == null)
        {
            instance = new UdpBroadcastManager();
        }
        return instance;
    }


    public void sendBroadcast(final Context context, final String message, final int port)
    {
                try {
                    broadcastAddress = getBroadcastAddress(context);
                    byte[] sendData = message.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, port);
                    Log.d("Udp","Sending broadcast: "+ message);
                    socket.send(sendPacket);
                } catch (IOException e) {
                    Log.e("Udp",e.toString());
                }

    }

    private InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow
        if(dhcp == null)
        {
            return InetAddress.getByName("255.255.255.255");
        }


        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

    public synchronized void startUdpListener(final Context context,int port)
    {

        if(listening)
        {
            Log.d("Udp","listening already working");
            return;
        }

        final ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi =
                connMgr.getActiveNetworkInfo();


        if(wifi.getType() != ConnectivityManager.TYPE_WIFI && !wifi.isAvailable()) {
            Log.d("udp","No Wifi network -- listener not started");
          return;
        }
            try {
                server = new DatagramSocket(port);
                server.setReuseAddress(true);
            //    server.setSoTimeout(15000);
            } catch (SocketException e) {
                Log.e("Udp",e.toString());
                return;
            }
            listening = true; //todo handle variable in threads
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Log.d("Udp","start listening");
                    final int bufferSize = 1024 * 5;
                    byte[] data = new byte[bufferSize];
                    while (listening) {
                        DatagramPacket packet = new DatagramPacket(data, bufferSize);
                        try {
                            server.receive(packet);
                        } catch (IOException e) {
                            Log.e("Udp", "UdpReceive exception + " + e.toString());
                            listening = false; //todo handle exception and think about restart listening
                            if(!server.isConnected())
                            {
                                server.close();
                            }
                            break;
                            //   sendBroadcast(context);
                        }
                        udpReceived(packet,context);
                        data = new byte[bufferSize];
                    }
                    Log.d("Udp", "Stopping UDP listener");
                    server.close();
                    server = null;
                    listening = false;
                }
            }).start();

    }



    public void udpReceived(DatagramPacket packet, Context context)
    {
        String pck = new String(packet.getData(),packet.getOffset(),packet.getLength());
        NetworkPackage np = new NetworkPackage();
        np.parsePackage(pck);
        if(np.getId().equals(android.os.Build.SERIAL))
        {
            return; // ignore my own packets
        }
        else if(np.getType().equals(BROADCAST_INTRODUCE))
        {
            neighboors.put(np.getId(),packet.getAddress()); // todo attention here !!
            Log.d("Udp","received package  + " + pck);
            Log.d("udp","number of connects " + Device.getConnectedDevices().size());
            if(!TcpConnectionManager.getInstance().checkExistingConnection(np.getId())) {
                TcpConnectionManager.getInstance().receivedUdpIntroduce(packet.getAddress(), BackgroundService.port, np,context);
            }
        }
    }


    public void stopUdpListener()
    {
        listening = false;
        if(server != null)
        server.close();
    }


    public synchronized void startSendingTask(final Context context, final int port) {

        if(!exitedFromSend)
        {
            sending = true;
            return;
        }
        startSending();
        exitedFromSend = false;
        NetworkPackage np = new NetworkPackage();
        try {
            final String message  = np.createFromTypeAndData(BROADCAST_INTRODUCE,BROADCAST_INTRODUCE_MSG); // todo handle null error
            if(socket == null) {
                socket = new DatagramSocket();
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
            }
        new Thread(new Runnable() {
            @Override
            public void run() {
                count = 0;
                    while (isSending()) {
                        sendBroadcast(context, message, port);
                        try {
                            count++;
                            Thread.sleep(getSend_period());
                            if(count == 10)
                            {
                              setSend_period(send_period*2);
                            }else if(count == 20)
                            {
                                setSend_period(send_period*2);
                            }

                        } catch (InterruptedException e) {
                            Log.e("Udp", "Error in sending loop");
                            break;
                        }
                    }
                    stopSending();
                exitedFromSend = true;
                }
        }).start();
        }
        catch (SocketException e)
        {
            Log.e("Udp",e.toString());
        }
    }
    public static boolean isSending() {return sending;}

    public static void startSending() {sending = true;}

    public static void stopSending()
    {
        sending = false;
    }

    public int getSend_period() {
        return send_period;
    }

    public void setSend_period(int send_period) {
        this.send_period = send_period;
    }
}
