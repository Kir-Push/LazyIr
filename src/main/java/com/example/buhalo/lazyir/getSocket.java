package com.example.buhalo.lazyir;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by buhalo on 22.01.17.
 */

public class getSocket extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {

        //Your code goes here
        final String host = "192.168.0.102";
        final int portNumber = 5667;
        System.out.println("Creating socket to '" + host + "' on port " + portNumber);
        try {
            System.out.println(InetAddress.getByName(host));
         //   Socket socket = new Socket("192.168.0.102", portNumber);
            Socket  socket = new Socket();
            socket.connect(new InetSocketAddress("192.168.0.102",portNumber),10000);
            System.out.println("nerowel");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


            System.out.println("yeap");
            String userInput = "please shutDown My Pc, Thank you";

            out.println("please shutDown My Pc, Thank you");

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("server says:" + br.readLine());

            if ("exit".equalsIgnoreCase(userInput)) {
                socket.close();
                return "";
            }
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("tut?");
            return "";
        }
        return "";
    }
}