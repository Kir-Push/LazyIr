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

       


    }
//todo add  checking wheter module enabled or not, and everything about it

    public synchronized void sendCommandToServer(final String id, final String command) {
        Device device =  Device.connectedDevices.get(id);
        if(device == null || !device.isConnected()) {
            Log.d("Tcp","Error in output for jasechsocket");
            StopListening(id);
            return;
        }if(!device.isPaired()) {
            Log.d("Tcp","Device is not paired and so on not allowed to continue");
            return;
        }
        device.printToOut(command);
        Log.d("Tcp", "Send command " + command);
    }

    public void sendPairing(String id)
    {
        NetworkPackage networkPackage = NetworkPackage.Cacher.getOrCreatePackage(TCP_PAIR,String.valueOf(android.os.Build.SERIAL.hashCode()));
        sendCommandToServer(id,networkPackage.getMessage());
    }

    //send unpair command to server
    // remove pair note from db
    public void unpair(String id,Context context) {
        DBHelper.getInstance(context).deletePaired(id);
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(TCP_UNPAIR, TCP_UNPAIR);
        sendCommandToServer(id,orCreatePackage.getMessage());
        Device.getConnectedDevices().get(id).setPaired(false);
        //   unPairAction(id); //todo
    }




    public void StopListening(String id) {
        Device closingDevice = Device.connectedDevices.get(id);
        if(closingDevice == null)
            return;
        closingDevice.closeConnection();
    }


    public boolean checkExistingConnection(String dvId) {
       if(!Device.getConnectedDevices().containsKey(dvId)) {
           return false;
       }
        if(Device.getConnectedDevices().get(dvId).getSocket() == null || !Device.getConnectedDevices().get(dvId).getSocket().isConnected()) {
            StopListening(dvId);
            return false;
        }return true;
    }
}
