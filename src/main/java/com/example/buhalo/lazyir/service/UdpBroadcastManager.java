package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.Exception.ParseError;
import com.example.buhalo.lazyir.MainActivity;

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

    private static final String BROADCAST_INTRODUCE = "broadcast introduce";
    private static final String BROADCAST_INTRODUCE_MSG = "I search Adventures";
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
        Log.d("Udp","Start sending broadcast + " + Thread.currentThread().getName());
                try {

                    broadcastAddress = getBroadcastAddress(context);
                  //  String messageStr = message + "::" + android_id + "::" + android_name;
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
                Log.e("Udp",e.toString());
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
                            Log.e("Udp", "UdpReceive exception + " + e.toString());
                            listening = false; //todo handle exception and think about restart listening
                            if(!server.isClosed())
                            {
                                server.close();
                            }
                            //   sendBroadcast(context);
                        }
                    }
                    Log.d("Udp", "Stopping UDP listener");
                    server.close();
                    server = null;
                    listening = false;
                }
            }).start();

    }



    public void broadcastReceived(DatagramPacket packet,Context context)
    {
        String pck = new String(packet.getData(),packet.getOffset(),packet.getLength());
        NetworkPackage np = new NetworkPackage();
        np.parsePackage(pck);
        if(np.getId().equals(android.provider.Settings.Secure.ANDROID_ID))
        {
            return; // ignore my own packets
        }
        else if(np.getType().equals(BROADCAST_INTRODUCE))
        {
            Log.d("Udp","Broadcast data received: " + np.getData());
            neighboors.add(packet.getAddress().toString()); // todo attention here !!
            Log.d("Udp","Id is " + np.getId() + " and name is" + np.getName());
            if(!TcpConnectionManager.getInstance().checkExistingConnection(np.getId())) {
                TcpConnectionManager.getInstance().receivedUdpIntroduce(packet.getAddress().toString().substring(1), BackgroundService.port, np.getId(), np.getName(),context);
            }
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
        sendBroadcast(context,BROADCAST_INTRODUCE_MSG,port);
    }




    public void startSendingTask(final Context context, final int port) {

        sending = true;
        NetworkPackage np = new NetworkPackage();
        try {
            final String message  = np.createFromTypeAndData(BROADCAST_INTRODUCE,BROADCAST_INTRODUCE_MSG);
            if(socket == null) {
                socket = new DatagramSocket();
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
            }
        new Thread(new Runnable() {
            @Override
            public void run() {
                    while (sending) {
                        sendBroadcast(context, message, port);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            Log.e("Udp", "Error in sending loop");
                            break;
                        }
                    }
                }
        }).start();
        } catch (ParseError parseError) {
            Log.e("Udp",parseError.getMessage()); // if parse error not start sending task // todo pridumaj 4tonibudj bljatj
        }
        catch (SocketException e)
        {
            Log.e("Udp",e.toString());
        }
    }

    public void stopSending()
    {
        sending = false;
    }
}
