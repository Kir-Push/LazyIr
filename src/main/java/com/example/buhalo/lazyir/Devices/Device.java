package com.example.buhalo.lazyir.Devices;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by buhalo on 19.02.17.
 */

public class Device {
    public static Map<String,Device> connectedDevices = new HashMap<>();
    private Socket socket;
    private String id;
    private String name;
    private InetAddress ip;
    private boolean paired;
    private BufferedReader in;
    private PrintWriter out;
    private boolean listening;

    public Device(Socket socket, String id, String name, InetAddress ip,BufferedReader in,PrintWriter out) {
        this.socket = socket;
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.paired = false;
        this.out = out;
        this.in = in;
        this.listening = false;
    }

    public static Map<String, Device> getConnectedDevices() {
        return connectedDevices;
    }

    public static void setConnectedDevices(Map<String, Device> connectedDevices) {
        Device.connectedDevices = connectedDevices;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }


    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }
}
