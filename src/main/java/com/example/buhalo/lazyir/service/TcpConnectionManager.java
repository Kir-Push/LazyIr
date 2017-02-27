package com.example.buhalo.lazyir.service;

import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by buhalo on 19.02.17.
 */

public class TcpConnectionManager {
    private static TcpConnectionManager instance;

    private TcpConnectionManager() {
    }

    public static TcpConnectionManager getInstance()
    {
        if(instance == null)
        {
            instance = new TcpConnectionManager();
        }
        return instance;
    }

    private Socket createSocket() // for future testing purposes
    {
        return new Socket();
    }

    public void receivedUdpIntroduce(final String address, final int port, final String id, final String name) {

        if(checkForOpenConnection(id))
        {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = createSocket();
                InetAddress inetAddress = null;
                BufferedReader in;
                PrintWriter out;
                String answer;
                String idTemp = id;
                try {

                    inetAddress = InetAddress.getByName(address);
                    Log.d("Tcp","I'm start connect via tcp to " + inetAddress);
                   // socket.setSoTimeout(10000);
                    socket.setKeepAlive(true);
                    socket.connect(new InetSocketAddress(address,port),10000);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream());
                    Device dv = new Device(socket,id,name, inetAddress,in,out);
                    dv.setListening(true);
                    Device.connectedDevices.put(address,dv);
                    MainActivity.selected_id = id; // only for testing;

                    while(dv.isListening())
                    {
                        Log.d("Tcp","I'w try to read from socket tcp");
                        if(!socket.isConnected() || socket.isClosed())
                            break;
                        try {
                            answer = in.readLine();
                            proceedAnswerFromServer(answer,id);
                        } catch (IOException e) {
                            Log.d("Tcp",e.toString());
                            StopListening(id);
                        }
                    }
                    Log.d("Tcp","I'w end socket reading!! for + " + id);

                } catch (IOException e) {
                  Log.d("Tcp","you have error in tcp connection");
                    StopListening(id);
                }
            }
        }).start();

    }

    private boolean checkForOpenConnection(String id) {
        Device dv = Device.connectedDevices.get(id);
        if(dv == null)
        {
            return false;
        }
        if( dv.getSocket() == null || !dv.getSocket().isConnected() || dv.getSocket().isClosed() || !dv.isListening())
        {
            StopListening(id);
            return false;
        }
        return true;
    }

    private void proceedAnswerFromServer(String answer, String id) {
        if(answer.equals("ping ping pong from: " + id))
        {
            sendCommandToServer(id,"pong pong ping");
        }
        if(answer.equals("pairing request from best friend"))
        {
            Device.connectedDevices.get(id).setPaired(true); // only for testing
        }
    }


    public void sendCommandToServer(final String id, final String command)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Device device =  Device.connectedDevices.get(id);
                if(device == null || device.getSocket() == null || !device.getSocket().isConnected() || device.getSocket().isClosed())
                {
                    Log.d("Tcp","Error in output for socket");
                    StopListening(id);
                    TryConnect(device);
                    return;
                }
                if(!device.isPaired())
                {
                    Log.d("Tcp","Device is not paired and so on not allowed to continue");
                    return;
                }
                try {

                    if(device.getOut() != null) {
                        device.getOut().println(command);
                        Log.d("Tcp", "Send command " + command);
                    }

                }catch (Exception e)
                {
                    Log.d("Tcp","Error in open output for socket " + e.toString());
                }
            }
        }).start();
    }

    public void StopListening(String id) {
        if(!Device.connectedDevices.containsKey(id))
            return;
        Device closingDevice = Device.connectedDevices.get(id);
        closingDevice.setListening(false);
        closingDevice.setPaired(false);
        try {
            Log.d("Tcp","Delete " + id + " connection");
            closingDevice.getIn().close();
            Device.connectedDevices.remove(id);
        } catch (IOException e) {
            Log.d("Tcp","Error in closing listening");
        }
    }

    private void TryConnect(Device device) {
        //create some timer or so
        receivedUdpIntroduce(device.getSocket().getInetAddress().toString(),device.getSocket().getPort(),device.getId(),device.getName());
    }
}
