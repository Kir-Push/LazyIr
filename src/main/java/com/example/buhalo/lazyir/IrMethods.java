package com.example.buhalo.lazyir;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.view.View;

import com.example.buhalo.lazyir.service.TcpConnectionManager;
import com.example.buhalo.lazyir.service.getSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by buhalo on 08.01.17.
 */
//dada
public class IrMethods {
    private static String SamsungPowerOffOn = "0000 006d 0022 0003 00a9 00a8 0015 003f 0015 003f 0015 003f 0015 " +
            "0015 0015 0015 0015 0015 0015 0015 " +
            "0015 0015 0015 003f 0015 003f 0015 003f 0015 0015 0015 0015 0015 0015 0015 0015" +
            " 0015 0015 0015 0015 0015 003f 0015 0015 0015 " +
            "0015 0015 0015 " +
            "0015 0015 0015 0015 0015 0015 0015 0040 0015 0015 0015 003f 0015 003f 0015 003f " +
            "0015 003f 0015 003f 0015 003f 0015 0702 00a9 00a8 0015 0015 0015 0e6e";

    private static ConsumerIrManager irManager;
    private static String tt = "0000 006d 0026 0000 0155 00aa 0016 0015 0016 0015 0016 0040 0016 0015 0016 0015 0016 0014 0016 0015 0016 0015 0016 0040 0016 0040 0016 0015 0016 0040 0016 0040 0016 0040 0016 0040 0016 0040 0016 0040 0016 0014 0016 0040 0016 0015 0016 0015 0016 0014 0016 0040 0016 0040 0016 0014 0016 0040 0016 0015 0016 0040 0016 0040 0016 0040 0016 0014 0016 0015 0016 060b 0155 0055 0016 0e58 0155 0055 0016 00aa";
    private static String LgPowerOnOff = "0000 006C 0022 0003 00AD 00AD 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 0041 0016 0016 0016 0016 0016 0016 0016 0041 0016 0016 0016 0016 0016 0016 0016 0016 0016 0041 0016 0041 0016 0041 0016 06FB 00AD 00AD 0016 0016 0016 0E98";
    private static final int[] SAMSUNG_POWER_TOGGLE_DURATION = {4495,4368,546,1638,546,1638,546,1638,546,546,546,546,546,546,546,546,546,546,546,1638,546,1638,546,1638,546,546,546,546,546,546,546,546,546,546,546,546,546,1638,546,546,546,546,546,546,546,546,546,546,546,546,546,1664,546,546,546,1638,546,1638,546,1638,546,1638,546,1638,546,1638,546,46644,4394,4368,546,546,546,96044};
    private static getSocket connection;

    public static void processOnlyTv(View view,Context context)
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
        List<String> list;
        transmit(SamsungPowerOffOn);
    }

    public static void processOnlyAudio(View view,Context context)
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
        List<String> list;
        transmit(LgPowerOnOff);
    }


    private static void transmit(String hexCode) {
        String data = hex2dec(hexCode);
        if (data != null) {
            String values[] = data.split(",");
            int[] pattern = new int[values.length - 1];

            System.out.println(values[0]);
            for (int i = 0; i < pattern.length; i++) {
                pattern[i] = Integer.parseInt(values[i + 1]);
                System.out.println(pattern[i]);
            }

            irManager.transmit(Integer.parseInt(values[0]), convertToToggleDuration(Integer.parseInt(values[0]), pattern));
        }

    }

    private static int[] convertToToggleDuration(int freq,int[] notConverted)
    {
        double pulse = 1000000 / freq;
        int[] converted = new int[notConverted.length];
        for(int i = 0;i<notConverted.length;i++)
        {
            converted[i] = (int)(notConverted[i] * pulse);
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

    public static void processOnlyPC(View view,Context context)
    {

        getSocket task = new getSocket();
        task.execute(new String[] { "ShutDown" });

    }

    public static void increaseVolume(View view,Context context)
    {
//        getSocket task = new getSocket();
//        task.setView(context);
//        task.execute(new String[] { "Volume+" });
        TcpConnectionManager instance = TcpConnectionManager.getInstance();
        instance.sendCommandToServer("1","please increase my volume, Thank you");
    }

    public static void decreaseVolume(View view,Context context)
    {

//        getSocket task = new getSocket();
//        task.setView(context);
//    task.execute(new String[] { "Volume-" });
        TcpConnectionManager instance = TcpConnectionManager.getInstance();
        instance.sendCommandToServer("1","please decrease my volume, Thank you");

    }

    public static void sendBroadcast(View view,Context context)
    {
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            String messageStr = "Hi,Can i meet you?: 192.168.0.1 port:995";
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(context), 5667);
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InetAddress getBroadcastAddress(Context mContext) throws IOException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
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



    private static getSocket setConnection()
    {
        if(connection == null)
        {
            connection = new getSocket();
        }
        return connection;
    }
}
