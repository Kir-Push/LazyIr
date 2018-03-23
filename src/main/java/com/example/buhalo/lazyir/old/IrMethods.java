package com.example.buhalo.lazyir.old;

import android.app.Activity;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by buhalo on 08.01.17.
 */
//dada
public class IrMethods {
    private static ConsumerIrManager irManager;
   // private static getSocket connection;



    private static void transmit(String code) {
        String data = hex2dec(code);
        if (data != null) {
            String values[] = data.split(",");
            int[] pattern = new int[values.length - 1];

            for (int i = 0; i < pattern.length; i++) {
                pattern[i] = Integer.parseInt(values[i + 1]);
            }

            irManager.transmit(Integer.parseInt(values[0]), convertToToggleDuration(Integer.parseInt(values[0]),pattern));
        }

    }

    private static int[] convertToToggleDuration(int freq,int[] notConverted)
    {
        double pulse = 1000000 / freq;
        int[] converted = new int[notConverted.length];
        for(int i = 0;i<notConverted.length;i++)
        {
            converted[i] = (int)(notConverted[i] * pulse);
            System.out.print(converted[i] + " ");
        }
        return converted;
    }

    private static String hex2dec(String irData) {
        List<String> list = new ArrayList<String>(Arrays.asList(irData
                .split(" ")));
        list.remove(0); // dummy
        int frequency = Integer.parseInt(list.remove(0), 16); // frequency
        list.remove(0); // seq1
        list.remove(0); // seq2

        for (int i = 0; i < list.size(); i++) {
            list.set(i, Integer.toString(Integer.parseInt(list.get(i), 16)));
        }

        frequency = (int) (1000000 / (frequency * 0.241246));
        list.add(0, Integer.toString(frequency));

        irData = "";
        for (String s : list) {
            irData += s + ",";
        }
        return irData;
    }





    public static InetAddress getBroadcastAddress(Context mContext) throws IOException {
        WifiManager wifi = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow


        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            System.out.println(quads[k]);
        }
        System.out.println(InetAddress.getByAddress(quads));
        return InetAddress.getByAddress(quads);
    }

    public static void processCommands(Context context,List<Command> commands)
    {
        for(Command command : commands)
        {
            if(irManager== null)
            {
                irManager= (ConsumerIrManager)context.getSystemService(Context.CONSUMER_IR_SERVICE);
            }
            System.out.println("yeap");
            if(irManager != null && irManager.hasIrEmitter())
            {
                System.out.println("you phone has ir, GOOD");
            }
            else
                return;
            transmit(command.getCommand());
         //   transmit(duration,);
        }

    }

    /**
     * Created by buhalo on 22.01.17.
     */

    public static class getSocket extends AsyncTask<String, Void, String> {
        private static String shutDownCommand = "please shutDown My Pc, Thank you";
        private static String increaseVol = "please increase my volume, Thank you";
        private static String decreaseVol = "please decrease my volume, Thank you";
        private static String host = "192.168.0.102";
        private static int portNumber = 5667;
        private static Socket socket;
        private static PrintWriter out;
        private static BufferedReader in;
        private  Context cOntext;
        private final static String[] cmds = {"ShutDown","Volume+","Volume-"};

        @Override
        protected String doInBackground(String... commands) {



           if(socket == null || socket.isClosed())
           {
               System.out.println("socket is closed");
               try {
                   System.out.println("i'm open connection");
                   openConnection();
               } catch (IOException e) {
                   e.printStackTrace();
                   return "error in openConnection";
               }
           }

            if(!socket.isConnected())
            {
                System.out.println("i'm in close connection!");
                closeConnection();
            }




            String answr = "";
            int attemptCount = 0;
            do {
                if(out == null)
                {
                    break;
                }
                System.out.println("i'm before command check");
                if (commands != null && commands.length > 0) {
                    if (commands[0].equals(cmds[0])) {
                        System.out.println("i'm in first command");
                        out.println(shutDownCommand);
                        answr = receiveFromServer();
                        System.out.println("i'm after receive from first command");
                    } else if (commands[0].equals(cmds[1])) {

                        System.out.println("i'm in second command");
                        out.println(increaseVol);
                        final String answer = receiveFromServer();
                        answr = answer;
                        System.out.println("i'm after receive from second command");
                        final Activity activity = (Activity) cOntext;
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, answer, Toast.LENGTH_SHORT).show();
                            }
                        });
                        // Toast.makeText(cOntext, "yrap", Toast.LENGTH_SHORT).show();
                        // receiveFromServer();
                    } else if (commands[0].equals(cmds[2])) {
                        System.out.println("i'm in third command");
                        out.println(decreaseVol);
                        final String answer = receiveFromServer();
                        answr = answer;
                        System.out.println("i'm after receive from third command");
                        final Activity activity = (Activity) cOntext;
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, answer, Toast.LENGTH_SHORT).show();
                            }
                        });
                        //   Toast.makeText(cOntext, answer, Toast.LENGTH_LONG).show();
                    }
                }
            } while (answr != null && answr.equals("try to reconnect!") && attemptCount++ < 2);
            System.out.println("i'm before return");
            return "";
        }

        private boolean openConnection() throws IOException {

            socket = new Socket();
            System.out.println("Creating socket to '" + host + "' on port " + portNumber);
            System.out.println(InetAddress.getByName(host));
            socket.connect(new InetSocketAddress("192.168.0.102",portNumber),10000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        }

        private boolean closeConnection()
        {
            if(out != null) {
                out.close();
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return false;
                    }
                }
            }
            socket = null;
            return true;
        }

        private String receiveFromServer()
        {
            String answer = "";
            if(in == null)
            {
                return answer;
            }
            try {
                 answer = in.readLine();
                if(answer == null)
                {
                    closeConnection();
                    openConnection();
                    answer = "try to reconnect!";
                }
                System.out.println("server says:" + answer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return answer;
        }

        public void setView(Context v)
        {
            this.cOntext = v;
        }
    }
}
