package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.Exception.TcpError;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.modules.ModuleExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

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

    public void receivedUdpIntroduce(final String address, final int port, final String id, final String name, final Context context) {

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
                    Device dv = new Device(socket,id,name, inetAddress,in,out,context);
                    dv.setListening(true);
                    Device.connectedDevices.put(id,dv);
                    MainActivity.selected_id = id; // only for testing;
                    UdpBroadcastManager.getInstance().stopSending();
                    UdpBroadcastManager.getInstance().stopUdpListener();   // todo only for testing
                    startPingPong(id);
                    while(dv.isListening())
                    {
                        Log.d("Tcp","I'w try to read from socket tcp " + Thread.currentThread().getName());
                        if(!socket.isConnected() || socket.isClosed() ) {
                            break;
                        }
                            answer = in.readLine();
                        if(answer == null || answer.isEmpty())
                        {
                            break;
                        }
                            proceedAnswerFromServer(answer,id,dv);
                    }
                    Log.d("Tcp","I'w end socket reading!! for + " + id);

                } catch (IOException e) {
                  Log.e("Tcp","you have error in tcp connection + "+ e);

                }finally {
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

    private synchronized void proceedAnswerFromServer(String answer, String id,Device dv) {
        if(dv == null || Device.connectedDevices.get(id) == null)
        {
            return;
        }
        if(answer.trim().equals("pong pong ping:" + id))
        {
            Log.d("Tcp","ping pong received for " + id);
            dv.setAnswer(true);
            return;
        }
        Log.d("Tcp","answer from tcp " + id + " is " + answer.trim());

        NetworkPackage np = new NetworkPackage();
        np.parsePackage(answer);
        np.setDv(dv);
        ModuleExecutor.executePackage(np);
    }


    public synchronized boolean sendCommandToServer(final String id, final String command) throws TcpError
    {
                Device device =  Device.connectedDevices.get(id);
                if(device != null)
                 {
                     device.setPaired(true);
                 }
                 // todo only for testing
                if(device == null || device.getSocket() == null || !device.getSocket().isConnected() || device.getSocket().isClosed())
                {
                    Log.d("Tcp","Error in output for socket");
                    StopListening(id);
                    TryConnect(device);
                    throw new TcpError("error in connection");
                }
                if(!device.isPaired())
                {
                    Log.d("Tcp","Device is not paired and so on not allowed to continue");
                   throw new TcpError("device is not paired");
                }
                try {

                    if(device.getOut() != null) {
                        device.getOut().println(command);
                        device.getOut().flush();
                        Log.d("Tcp", "Send command " + command);
                    }

                }catch (Exception e)
                {
                    Log.d("Tcp","Error in open output for socket " + e.toString());
                    throw new TcpError("error in open output for socket");
                }
        return true;
    }

    public synchronized void sendCommandsToServerSeparateThread(final String id, final List<String> commands)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(String command : commands)
                {
                    try {
                        sendCommandToServer(id,command);
                    } catch (TcpError tcpError) {
                        tcpError.printStackTrace(); // todo handle exceptions
                    }
                }
            }
        }).start();
    }

    public void StopListening(String id) {
        if(!Device.connectedDevices.containsKey(id))
            return;
        Device closingDevice = Device.connectedDevices.get(id);
        if(closingDevice == null)
        {
            Device.connectedDevices.remove(id);
            return;
        }
        closingDevice.setListening(false);
        closingDevice.setPaired(false);
        try {
            Log.d("Tcp","Delete " + id + " connection");
            Device.connectedDevices.remove(id);
            closingDevice.getSocket().close();
        } catch (IOException e) {
            Log.e("Tcp","Error in closing listening");
        }
    }

    private void TryConnect(Device device) { // todo ?? create this class ??
        //create some timer or so
        if(device == null || device.getSocket() == null)
        {
            device = null;
            return;
        }
        if(checkExistingConnection(device.getId()))
        {
            StopListening(device.getId());
        }
        receivedUdpIntroduce(device.getSocket().getInetAddress().toString(),device.getSocket().getPort(),device.getId(),device.getName(),device.getContext());
    }

    private void startPingPong(final String id)
    {
        Device.getConnectedDevices().get(id).setPinging(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(Device.getConnectedDevices().get(id) != null  && Device.getConnectedDevices().get(id).isPinging())
                {
                    try {
                        Device.getConnectedDevices().get(id).setAnswer(false);
                        sendCommandToServer(id,"ping ping pong");
                    } catch (TcpError tcpError) {
                        Log.d("Tcp",tcpError.getMessage());
                        break;
                    }
                    try {
                        Thread.sleep(10000);
                        Log.d("Tcp","ping pong after sleep " + Thread.currentThread().getName());
                        if(Device.getConnectedDevices().get(id) == null || !Device.getConnectedDevices().get(id).isAnswer())
                        {
                       //     TryConnect(Device.getConnectedDevices().get(id));
                            StopListening(id);
                            break;
                        }
                    } catch (InterruptedException e) {
                        Log.d("Tcp",e.toString());
                        break;
                    }
                }
                if(Device.getConnectedDevices().get(id) != null)
                {
                    Device.getConnectedDevices().get(id).setPinging(false);
                }
            }
        }).start();
    }

    public boolean checkExistingConnection(String dvId)
    {
       if(!Device.getConnectedDevices().containsKey(dvId))
       {
           return false;
       }
        if(Device.getConnectedDevices().get(dvId).getSocket() == null || !Device.getConnectedDevices().get(dvId).getSocket().isConnected())
        {
            StopListening(dvId);
            return false;
        }
        return true;

    }
}
