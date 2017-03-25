package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
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
import java.util.ArrayList;
import java.util.List;

import static com.example.buhalo.lazyir.service.BackgroundService.port;

/**
 * Created by buhalo on 19.02.17.
 */

public class TcpConnectionManager {
    public final static String TCP_INTRODUCE = "tcpIntroduce";
    public final static  String TCP_INTRODUCE_MSG = "my name is";
    public final static String TCP_PING = "ping pong";
    public final static String TCP_PAIR_RESULT = "pairedresult";
    public final static String OK = "ok";
    public final static String REFUSE = "refuse";
    public final static String TCP_PAIR = "pair";
    public final static String TCP_UNPAIR = "unpair";
    public final static String TCP_SYNC = "sync";
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
                        socket.setKeepAlive(true);

                    } catch (IOException e) {
                        Log.e("Tpc","Exception on accept connection ignoring + " + e.toString());
                        if(myServerSocket.isClosed())
                            ServerOn = false;
                        continue;
                    }
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

    public void receivedUdpIntroduce(InetAddress address, int port,NetworkPackage np, Context context) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address,port),10000);
            socket.setKeepAlive(true);
            ConnectionThread connection = new ConnectionThread(socket,context);
            connection.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public class ConnectionThread extends Thread {

        private Socket connection;
        private Context context;
        private String deviceId = null;
        private boolean connectionRun = true;
        BufferedReader in = null;
        PrintWriter out = null;
        private boolean hasPaired = false;
        private boolean waitAnswerPair = false;

        public ConnectionThread(Socket socket,Context context) {
            this.connection = socket;
            this.context = context;
        }

        @Override
        public void run() {
            Log.d("Tcp","Start connecting to new connection");
            try{
                in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                out = new PrintWriter(
                        new OutputStreamWriter(connection.getOutputStream()));

                if(Device.getConnectedDevices().size() == 0) //if first connection set periods of broadcast 3 times less often
                {
               //     UdpBroadcastManager.getInstance().setSend_period(UdpBroadcastManager.getInstance().getSend_period()*3);
                }
                while (connectionRun)
                {
                    String clientCommand = in.readLine();
                    Log.d("Tcp","Client says.. " + clientCommand);


                    if(clientCommand == null)
                    {
                        connectionRun = false;
                        continue;
                    }
                    NetworkPackage np = new NetworkPackage();
                    np.parsePackage(clientCommand);
                    determineWhatTodo(np);
                }

            }catch (Exception e)
            {
                Log.e("Tcp","Error in tcp out + " + e.toString());
            }
            finally {
                try {
                    in.close();
                    out.close();
                    connection.close();
                    StopListening(deviceId);
                    if(Device.getConnectedDevices().size() == 0) //return to normal frequency
                    {
                        UdpBroadcastManager.getInstance().setSend_period(15000);
                        UdpBroadcastManager.getInstance().count = 0;
                    }
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
                    ping(null);
                    break;
                case TCP_PAIR_RESULT:
                    pairResult(np);
                    break;
                case TCP_SYNC:
                    receiveSync(np);
                    break;
                default:
                    Device.connectedDevices.get(deviceId).setAnswer(true);
                    commandFromClient(np);
                    break;
            }
        }

        private void receiveSync(NetworkPackage np) {
            try {

                List<String> args = np.getArgs();
                List<Command> receivedCommands = new ArrayList<>();
                for (int i = 1; i < args.size(); i++) {
                    String arg = args.get(i);
                    String[] split = arg.split(":;c:;");
                    receivedCommands.add(new Command(split[0], split[1], null, "pc"));
                }
                DBHelper.getInstance(context).removeCommandsPcAll();
                for (Command receivedCommand : receivedCommands) {
                    DBHelper.getInstance(context).saveCommand(receivedCommand);
                }
            }catch (Exception e)
            {
                Log.e("Tcp",e.toString());
            }


        }

        public void ping(NetworkPackage np)
        {
            NetworkPackage p = new NetworkPackage();
            //   Device.getConnectedDevices().get(deviceId).setAnswer(true);
            String msg = p.createFromTypeAndData(TCP_PING,TCP_PING);
            Log.d("Tcp","Send " + msg);
            out.println(msg);
            out.flush();
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

                if(np.getArgs().size() > 1)
                {
                    String pairedCode = np.getArgs().get(1);
                    List<String> savedPairedCode = DBHelper.getInstance(context).getPairedCode(deviceId);
                    if(savedPairedCode.size() != 0 && savedPairedCode.get(0).equals(pairedCode))
                    {
                        hasPaired = true;
                    }
                }
                sendIntroduce();
              //  startPingPong(deviceId);
            }
        }

        public void pairResult(NetworkPackage np)
        {
            if(np.getArgs() != null && np.getArgs().size() > 1 && deviceId != null)
            {
                if(np.getArgs().get(1).equals(OK))
                {

                    if(np.getArgs().size() > 2)
                    {
                        DBHelper.getInstance(context).savePairedDevice(deviceId,np.getArgs().get(2)); // todo check if exist already
                    }
                    Device.getConnectedDevices().get(deviceId).setPaired(true);

                }
            }
            hasPaired = false;
        }




        private void sendIntroduce() {
            NetworkPackage networkPackage = new NetworkPackage();
            if(hasPaired) {
                List<String> args = new ArrayList<>();
                args.add(String.valueOf(android.os.Build.SERIAL.hashCode()));
                networkPackage.setArgs(args);
            }
                out.println(networkPackage.createFromTypeAndData(TCP_INTRODUCE,TCP_INTRODUCE_MSG));
                out.flush();
        }

        public void commandFromClient(NetworkPackage np)
        {
            try {
                Module module = Device.getConnectedDevices().get(np.getId()).getEnabledModules().get(np.getType());
                module.execute(np);
            }catch (Exception e)
            {
                Log.e("Tcp",e.toString());
            }
        }
    }



    public synchronized boolean sendCommandToServer(final String id, final String command) throws TcpError
    {
                Device device =  Device.connectedDevices.get(id);
                if(device != null)
                 {
               //      device.setPaired(true);
                 }
                 // todo only for testing
                if(device == null || device.getSocket() == null || !device.getSocket().isConnected() || device.getSocket().isClosed())
                {
                    Log.d("Tcp","Error in output for socket");
                    StopListening(id);
                   // TryConnect(device);
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

    public void sendPairing(String id)
    {
        NetworkPackage networkPackage = new NetworkPackage();
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(android.os.Build.SERIAL.hashCode()));
        networkPackage.setArgs(args);
        Device device = Device.getConnectedDevices().get(id);
        device.getOut().println(networkPackage.createFromTypeAndData(TCP_PAIR,TCP_PAIR));
        device.getOut().flush();
    }

    public void unpair(String id,Context context)
    {
        NetworkPackage networkPackage = new NetworkPackage();
        PrintWriter out = Device.getConnectedDevices().get(id).getOut();
        DBHelper.getInstance(context).deletePaired(id);
        String fromTypeAndData = networkPackage.createFromTypeAndData(TCP_UNPAIR, TCP_UNPAIR);
        try {
            sendCommandToServer(id,fromTypeAndData);
        } catch (TcpError tcpError) {
            Log.d("Tcp",tcpError.toString());
        }
        Device.getConnectedDevices().get(id).setPaired(false);
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
