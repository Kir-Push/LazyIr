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
import java.nio.CharBuffer;

/**
 * Created by buhalo on 22.01.17.
 */

public class getSocket extends AsyncTask<String, Void, String> {
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
                    final String answer = receiveFromServer();
                    answr = answer;
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