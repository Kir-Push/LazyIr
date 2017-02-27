package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by buhalo on 19.02.17.
 */

public class UdpBroadcastManager  {

    private DatagramSocket socket;
    private  DatagramSocket server;
    private InetAddress broadcastAddress;
    private String android_id;
    private String android_name;


    public static boolean listening = false;
    public static boolean sending = false;
    private static UdpBroadcastManager instance;

    public static ArrayList<String> neighboors = new ArrayList<>();

    private UdpBroadcastManager() {
        try {
            configureManager();
        } catch (IOException e) {
            Log.d("Udp","Error in udp configure method");
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
        Log.d("Udp","Start sending broadcast + " + Thread.currentThread().getName());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    broadcastAddress = getBroadcastAddress(context);
                    if(android_id == null)
                    {
                        android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    }
                    if(android_name == null)
                    {
                        android_name = android.os.Build.MODEL;
                    }
                    String messageStr = message + "::" + android_id + "::" + android_name;
                    byte[] sendData = messageStr.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, port);
                    Log.d("Udp","Sending broadcast: "+ messageStr);
                    socket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("Udp",e.toString());
                }
            }
        }).start();
    }

    private InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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

    public void startUdpListener(final Context context,int port)
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
            neighboors = new ArrayList<>();
            try {
                server = new DatagramSocket(port);
                server.setReuseAddress(true);
            //    server.setSoTimeout(15000);
            } catch (SocketException e) {
                Log.d("Udp",e.toString());
                return;
            }
            listening = true;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Log.d("Udp","start listening");
                    final int bufferSize = 1024 * 5;
                    byte[] data = new byte[bufferSize];
                    while (listening) {
                        Log.d("Udp","listening Iteration");

                        DatagramPacket packet = new DatagramPacket(data, bufferSize);
                        try {
                            server.receive(packet);
                            broadcastReceived(packet,context);
                            data = new byte[bufferSize];
                        } catch (Exception e) {
                            Log.d("Udp", "UdpReceive exception + " + e.toString());
                            listening = false;
                            if(!server.isClosed())
                            {
                                server.close();
                            }
                            //   sendBroadcast(context);
                        }
                    }
                    Log.w("Udp", "Stopping UDP listener");
                    server.close();
                    listening = false;
                }
            }).start();

    }



    public void broadcastReceived(DatagramPacket packet,Context context)
    {
        String pck = new String(packet.getData(),packet.getOffset(),packet.getLength());
        Log.d("Udp","Received some broadcast ");
        if(pck.startsWith("I search Adventures:"))
        {
            Log.d("Udp","Upps it's my own broadcast");
            // ignome my own packets
            return;
        }
        else if(pck.startsWith("Broadcast message: hello,'m have some"))
        {
            Log.d("Udp","Broadcast from: " + packet.getAddress().toString());
            Log.d("Udp","Broadcast data received: " + pck);
            neighboors.add(packet.getAddress().toString());
            String dvId = pck.split("::")[1];
            String dvName = pck.split("::")[2];
            TcpConnectionManager.getInstance().receivedUdpIntroduce(packet.getAddress().toString(),packet.getPort(),dvId,dvName);
            //TODO // create handle for null pointers in dvID and dvName;
        }
    }


    public void stopUdpListener()
    {
        listening = false;
        if(server != null)
        server.close();
    }

    public void onNetworkChange(Context context,int port)
    {
        Log.d("Udp","OnNetworkChange method start");
        startUdpListener(context,port);
        sendBroadcast(context,"I search Adventures:",port);
    }




    public void startSendingTask(final Context context, final int port) {

        sending = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(sending)
                {
                    sendBroadcast(context,"I search Adventures",port);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Log.d("Udp","Error in sending loop");
                    }
                }
            }
        }).start();
    }

    public void stopSending()
    {
        sending = false;
    }
}
