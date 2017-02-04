package com.example.buhalo.lazyir;

import android.app.Activity;
import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        getSocket task = new getSocket();
        task.setView(context);
        task.execute(new String[] { "Volume+" });
    }

    public static void decreaseVolume(View view,Context context)
    {

        getSocket task = new getSocket();
        task.setView(context);
    task.execute(new String[] { "Volume-" });

    }
}
