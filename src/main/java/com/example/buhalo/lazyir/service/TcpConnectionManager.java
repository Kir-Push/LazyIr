package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.Exception.ParseError;
import com.example.buhalo.lazyir.Exception.TcpError;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.ModuleExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import static com.example.buhalo.lazyir.service.BackgroundService.port;

/**
 * Created by buhalo on 19.02.17.
 */

public class TcpConnectionManager {
    public final static String TCP_INTRODUCE = "tcpIntroduce";
    public final static  String TCP_INTRODUCE_MSG = "my name is";
    public final static String TCP_PING = "ping pong";
    private static TcpConnectionManager instance;

    private static volatile boolean ServerOn = false;
    ServerSocket myServerSocket;

    private TcpConnectionManager() {
        try {
            myServerSocket = new ServerSocket(port);
        } catch (IOException e) {
            Log.e("Tcp", e.toString());
        }
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

    public void startListening(final int port, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(ServerOn)
                {
                    Log.d("Tcp","Server already working");
                    return;
                }

                if(myServerSocket.isClosed())
                {
                    try {
                        myServerSocket = new ServerSocket(port);
                    } catch (IOException e) {
                        Log.e("Tcp",e.toString());
                    }
                }
                ServerOn = true;

                while(ServerOn)
                {
                    Socket socket;
                    try {
                        socket = myServerSocket.accept();


                    } catch (IOException e) {
                        Log.e("Tpc","Exception on accept connection ignoring + " + e.toString());
                        if(myServerSocket.isClosed())
                            ServerOn = false;
                        continue;
                    }
                    UdpBroadcastManager.stopSending(); // todo for testing
                    ConnectionThread connection = new ConnectionThread(socket,context);
                    connection.start();
                }
                try
                {
                    myServerSocket.close();
                    Log.d("Tcp","Closing server");
                }catch (IOException e)
                {
                    Log.e("Tcp","error in closing connecton");
                }
            }
        }).start();
    }

    public void stopListening() {
        ServerOn = false;
        try {
            myServerSocket.close();
        } catch (IOException e) {
            Log.e("Tcp","Error in close serverSocket " + e.toString());
        }
    }


    public class ConnectionThread extends Thread {

        private Socket connection;
        private Context context;
        private String deviceId = null;
        private boolean connectionRun = true;
        BufferedReader in = null;
        PrintWriter out = null;

        public ConnectionThread(Socket socket,Context context) {
            this.connection = socket;
        }

        @Override
        public void run() {
            Log.d("Tcp","Start connecting to new connection");
            try{
                in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                out = new PrintWriter(
                        new OutputStreamWriter(connection.getOutputStream()));

                while (connectionRun)
                {
                    String clientCommand = in.readLine();
                    Log.d("Tcp","Client says.. " + clientCommand);

                    if(!ServerOn) {
                        out.println("Server has already stopped");
                        out.flush();
                        connectionRun = false;
                    }

                    if(clientCommand == null)
                    {
                        connectionRun = false;
                        continue;
                    }
                    NetworkPackage np = new NetworkPackage();
                    np.parsePackage(clientCommand);
                    determineWhatTodo(np);
                }

            }catch (IOException e)
            {
                Log.e("Tcp","Error in tcp out + " + e.toString());
            }
            finally {
                try {
                    in.close();
                    out.close();
                    connection.close();
                    Log.d("Tcp","Stopped connection");
                }catch (IOException e)
                {
                    Log.e("Tcp",e.toString());
                }
            }
        }

        public void determineWhatTodo(NetworkPackage np)
        {
            switch (np.getType())
            {
                case TCP_INTRODUCE:
                    newConnectedDevice(np);
                    break;
                case TCP_PING:
                    Device.connectedDevices.get(deviceId).setAnswer(true);
                    break;
                default:
                    Device.connectedDevices.get(deviceId).setAnswer(true);
                    commandFromClient(np);
                    break;
            }
        }

        public void newConnectedDevice(NetworkPackage np) //todo create pairing in java server side and the of course
        {
            if(deviceId == null) {
                Device device = new Device(connection, np.getId(), np.getName(), connection.getInetAddress(), in, out,context);
                deviceId = np.getId();
                Device.getConnectedDevices().put(device.getId(), device);
                if(MainActivity.selected_id.equals(""))
                {
                    MainActivity.selected_id = deviceId;
                }
                sendIntroduce();
                startPingPong(deviceId);
            }
        }

        public void ping(NetworkPackage np)
        {
            NetworkPackage p = new NetworkPackage();
            try {
                String msg = p.createFromTypeAndData(TCP_PING,TCP_PING);
                out.println(msg);
                out.flush();
            } catch (ParseError parseError) {
                Log.e("Tcp",parseError.toString());
            }
        }

        private void sendIntroduce() {
            NetworkPackage networkPackage = new NetworkPackage();
            try {
                out.println(networkPackage.createFromTypeAndData(TCP_INTRODUCE,TCP_INTRODUCE_MSG));
                out.flush();
            } catch (ParseError parseError) {
                parseError.printStackTrace();
            }
        }

        public void commandFromClient(NetworkPackage np)
        {
            Module module = Device.getConnectedDevices().get(np.getId()).getEnabledModules().get(np.getType());
            module.execute(np);
        }
    }

    public synchronized void receivedUdpIntroduce(final String address, final int port, final String id, final String name, final Context context) {

        if(checkForOpenConnection(id))
        {
            return;
        }
        try {

            final Socket socket = createSocket();
            InetAddress inetAddress = null;
            final BufferedReader in;
            PrintWriter out;
            String idTemp = id;
            inetAddress = InetAddress.getByName(address);
            Log.d("Tcp", "I'm start connect via tcp to " + inetAddress);
            // socket.setSoTimeout(10000);
            socket.setKeepAlive(true);
            socket.connect(new InetSocketAddress(address, port), 10000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            final Device dv = new Device(socket, id, name, inetAddress, in, out, context);
            dv.setListening(true);
            Device.connectedDevices.put(id, dv);
            NetworkPackage networkPackage = new NetworkPackage();
            try {
                out.println(networkPackage.createFromTypeAndData(TCP_INTRODUCE,TCP_INTRODUCE_MSG));
            } catch (ParseError parseError) {
                parseError.printStackTrace();
            }
            MainActivity.selected_id = id; // only for testing;
            UdpBroadcastManager.getInstance().stopSending();       // todo only for testing
   //         UdpBroadcastManager.getInstance().stopUdpListener();   // todo only for testing
            startPingPong(id);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String answer;
                        while (dv.isListening()) {
                            Log.d("Tcp", "I'w try to read from socket tcp " + Thread.currentThread().getName());
                            if (!socket.isConnected() || socket.isClosed()) {
                                break;
                            }
                            answer = in.readLine();
                            if (answer == null || answer.isEmpty()) {
                                break;
                            }
                            proceedAnswerFromServer(answer, id, dv);
                        }
                        Log.d("Tcp", "I'w end socket reading!! for + " + id);

                    } catch (IOException e) {
                        Log.e("Tcp", "you have error in tcp connection + " + e);

                    } finally {
                        StopListening(id); // todo// create some check if dv user other connection or not , and not delete if use (or not delete it from connected in this method) , basically i think create in device array in which storage opened connections(create some class connection or so)
                        //todo// and if connection closed or lost delete from it,and when open connection add, and check if no connection's delete device, also try to reestablish listeing, and so.
                        UdpBroadcastManager.getInstance().startSendingTask(context,5667);       // todo only for testing
                  //      UdpBroadcastManager.getInstance().stopUdpListener();
                    }
                }
            }).start();
        }catch (IOException e)
        {
            Log.e("Tcp",e.toString());
        }

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

        if(np.getType().equals(TCP_PING))
        {
            Log.d("Tcp","ping pong received for " + id);
            dv.setAnswer(true);
            return;
        }

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
//        receivedUdpIntroduce(device.getSocket().getInetAddress().toString(),device.getSocket().getPort(),device.getId(),device.getName(),device.getContext());
    }

    private void startPingPong(final String id)
    {
        Device.getConnectedDevices().get(id).setPinging(true);
        NetworkPackage np = new NetworkPackage();
        String msg= null;
        try {
          msg = np.createFromTypeAndData(TCP_PING,TCP_PING);
        } catch (ParseError parseError) {
            parseError.printStackTrace();
            return;
        }
        final String msgf = msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(Device.getConnectedDevices().get(id) != null  && Device.getConnectedDevices().get(id).isPinging())
                {
                    try {
                        Device.getConnectedDevices().get(id).setAnswer(false);
                        sendCommandToServer(id,msgf);
                    } catch (TcpError tcpError) {
                        Log.d("Tcp",tcpError.getMessage());
                        break;
                    }
                    try {
                        Thread.sleep(30000);
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
