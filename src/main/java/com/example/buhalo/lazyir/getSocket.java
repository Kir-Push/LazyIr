package com.example.buhalo.lazyir;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by buhalo on 22.01.17.
 */

public class getSocket extends AsyncTask<String, Void, String> {
    private static String shutDownCommand = "please shutDown My Pc, Thank you";
    private static String increaseVol = "please increase my volume, Thank you";
    private static String decreaseVol = "please decrease my volume, Thank you";
    private static String host = "192.168.0.102";
    private static int portNumber = 5667;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Context cOntext;
    private final static String[] cmds = {"ShutDown","Volume+","Volume-"};

    @Override
    protected String doInBackground(String... commands) {

       if(socket == null || !socket.isConnected())
       {
           try {
               openConnection();
           } catch (IOException e) {
               e.printStackTrace();
               return "error in openConnection";
           }
       }

        if(commands != null && commands.length > 0)
        {
           if(commands[0].equals(cmds[0]))
           {
               out.println(shutDownCommand);
               receiveFromServer();
           }
            else if(commands[0].equals(cmds[1]))
           {

               out.println(increaseVol);
            final String   answer = receiveFromServer();
               final  Activity activity = (Activity) cOntext;
               activity.runOnUiThread(new Runnable() {
                   public void run()
                   {
                       Toast.makeText(activity, answer, Toast.LENGTH_SHORT).show();
                   }
               });
              // Toast.makeText(cOntext, "yrap", Toast.LENGTH_SHORT).show();
              // receiveFromServer();
           }
            else if(commands[0].equals(cmds[2]))
           {
               out.println(decreaseVol);
               final String    answer = receiveFromServer();
             final  Activity activity = (Activity) cOntext;
               activity.runOnUiThread(new Runnable() {
                   public void run()
                   {
                       Toast.makeText(activity, answer, Toast.LENGTH_SHORT).show();
                   }
               });
            //   Toast.makeText(cOntext, answer, Toast.LENGTH_LONG).show();
           }
        }

        closeConnection();
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
        return true;
    }

    private String receiveFromServer()
    {
        String answer = "";
        try {
             answer = in.readLine();
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