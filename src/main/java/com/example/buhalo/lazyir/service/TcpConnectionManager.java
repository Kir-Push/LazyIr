package com.example.buhalo.lazyir.service;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.CommandsList;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.battery.Battery;
import com.example.buhalo.lazyir.modules.shareManager.ShareModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static com.example.buhalo.lazyir.Devices.NetworkPackage.DEVICE_TYPE;
import static com.example.buhalo.lazyir.service.BackgroundService.port;

/**
 * Created by buhalo on 19.02.17.
 */

public class TcpConnectionManager {
    public final static String TCP_INTRODUCE = "tcpIntroduce";
    public final static String TCP_PING = "ping pong";
    public final static String TCP_PAIR_RESULT = "pairedresult";
    public final static String RESULT = "result";
    public final static String OK = "ok";
    public final static String REFUSE = "refuse";
    public final static String TCP_PAIR = "pair";
    public final static String TCP_UNPAIR = "unpair";
    public final static String TCP_SYNC = "sync";
    public static TcpConnectionManager instance;

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

    protected Socket getConnection(InetAddress ip, int port,Context context) throws IOException  {
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.testkeys);
            trustStore.load(trustStoreStream, "bimkaSamokat".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLSocketFactory factory = sslContext.getSocketFactory();
            return factory.createSocket(ip, port);
        } catch (GeneralSecurityException e) {
            Log.e(this.getClass().toString(), "Exception while creating context: ", e);
            throw new IOException("Could not connect to SSL Server", e);
        }
    }





    public void receivedUdpIntroduce(InetAddress address, int port,NetworkPackage np, Context context) {
        Socket socket = null;
        try {
            // at this moment connect only to pc
            if(!np.getValue(DEVICE_TYPE).equals("pc"))
                return;
        //    Socket socket = new Socket();
            socket = getConnection(address,port,context);
            System.out.println(socket.isConnected());
         //   socket.connect(new InetSocketAddress(address,port),10000);
            socket.setSoTimeout(60000);
            socket.setKeepAlive(true);
            ConnectionThread connection = new ConnectionThread(socket,context);
            connection.start();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if(socket!= null)
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void sendCommandToAll(String message) {
        if(Device.getConnectedDevices().size() == 0)
        {
            return;
        }
        for (Device device : Device.getConnectedDevices().values()) {
            sendCommandToServer(device.getId(),message);
        }

    }
 // todo locks and refactoringg like server side

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

//                if(Device.getConnectedDevices().size() == 0) //if first connection set periods of broadcast 3 times less often
//                {
//               //     UdpBroadcastManager.getInstance().setSend_period(UdpBroadcastManager.getInstance().getSend_period()*3);
//                }
                while (connectionRun)
                {
                    String clientCommand = in.readLine();
                    Log.d("Tcp","Client says.. " + clientCommand);


                    if(clientCommand == null)
                    {
                        connectionRun = false;
                        continue;
                    }
                    NetworkPackage np = new NetworkPackage(clientCommand);
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
                        MainActivity.selected_id = "";
                        ShareModule.stopSftpServer();
                    }
                    else
                    {
                        MainActivity.selected_id = Device.getConnectedDevices().values().iterator().next().getId();
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

                List<Command> receivedCommands = np.getObject(NetworkPackage.N_OBJECT, CommandsList.class).getCommands();
                if(receivedCommands == null)
                    return;
              //  DBHelper.getInstance(context).removeCommandsPcAll();
                List<String> commandsPc = DBHelper.getInstance(context).getCommandsPc();
                HashSet<String> pcSet = new HashSet<>(commandsPc);
                for (Command receivedCommand : receivedCommands) {
                    if(!pcSet.contains(receivedCommand.getCommand_name()))
                    DBHelper.getInstance(context).saveCommand(receivedCommand);
                }
            }catch (Exception e)
            {
                Log.e("Tcp",e.toString());
            }


        }

        public void ping(NetworkPackage np)
        {
            NetworkPackage p = new NetworkPackage(TCP_PING,TCP_PING);
            //   Device.getConnectedDevices().get(deviceId).setAnswer(true);
            String msg = p.getMessage();
            Log.d("Tcp","Send " + msg);
            out.println(msg);
            out.flush();
        }


        public void newConnectedDevice(NetworkPackage np)
        {
            if(deviceId == null) {
                Device device = new Device(connection, np.getId(), np.getName(), connection.getInetAddress(), in, out,context);
                deviceId = np.getId();
                Device.getConnectedDevices().put(device.getId(), device);
                if(MainActivity.selected_id.equals(""))
                {
                    MainActivity.selected_id = deviceId;
                }

                if(np.getData() != null)
                {
                    String pairedCode = np.getData();
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
                    hasPaired = false;
                }
            }
            else {
                hasPaired = false;
            }
        }




        private void sendIntroduce() {
            String data= null;
            if(hasPaired) {
                data = String.valueOf(android.os.Build.SERIAL.hashCode());
            }
            NetworkPackage networkPackage = new NetworkPackage(TCP_INTRODUCE,data);
                out.println(networkPackage.getMessage());
                out.flush();
        }

        public void commandFromClient(NetworkPackage np)
        {
            try {
                Device device = Device.getConnectedDevices().get(np.getId());
                if(!device.isPaired())
                {
                    return;
                }
                Module module = device.getEnabledModules().get(np.getType());
                module.execute(np);
            }catch (Exception e)
            {
                Log.e("Tcp",e.toString());
            }
        }
    }
//todo add  checking wheter module enabled or not, and everything about it

//todo lock as in server and so on
    public synchronized boolean sendCommandToServer(final String id, final String command)
    {
                Device device =  Device.connectedDevices.get(id);

                if(device == null || device.getSocket() == null || !device.getSocket().isConnected() || device.getSocket().isClosed())
                {
                    Log.d("Tcp","Error in output for socket");
                    StopListening(id);
                    return true; // if device null or not connected stopping listening and return from method, or may cause nullpointer exception later in method
                   // TryConnect(device);
                }
                if(!device.isPaired()) // todo here eror check
                {
                    Log.d("Tcp","Device is not paired and so on not allowed to continue");
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
                }
        return true;
    }

    public void sendPairing(String id)
    {
        NetworkPackage networkPackage = new NetworkPackage(TCP_PAIR,String.valueOf(android.os.Build.SERIAL.hashCode()));
        Device device = Device.getConnectedDevices().get(id);
        if(device == null) {
            StopListening(id);
            return;
        }
        device.getOut().println(networkPackage.getMessage());
        device.getOut().flush();
    }

    public void unpair(String id,Context context)
    {
        NetworkPackage networkPackage = new NetworkPackage(TCP_UNPAIR, TCP_UNPAIR);
        Device device = Device.getConnectedDevices().get(id);

        DBHelper.getInstance(context).deletePaired(id);
        String fromTypeAndData = networkPackage.getMessage();
        sendCommandToServer(id,fromTypeAndData);
        if(device == null)
        {
            StopListening(id);
            return;
        }
        Device.getConnectedDevices().get(id).setPaired(false);
     //   unPairAction(id); //todo
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
