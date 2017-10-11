package com.example.buhalo.lazyir.modules.dbus;

/**
 * Created by buhalo on 15.04.17.
 */

public class Player {
    private String name;
    private String playbackStatus;
    private String title;
    private double lenght;
    private double volume;
    private double currTime;
    private String readyTimeString;
    private String type;
    private String id;

    public Player(String name, String playbackStatus, String title, double lenght, double volume, double currTime, String readyTimeString) {
        this.name = name;
        this.playbackStatus = playbackStatus;
        this.title = title;
        this.lenght = lenght;
        this.volume = volume;
        this.currTime = currTime;
        this.readyTimeString = readyTimeString;
        this.type = "dbus";
        this.id = "-1";
    }

    public Player(String name, String playbackStatus, String title, double lenght, double volume, double currTime, String readyTimeString, String type, String id) {
        this.name = name;
        this.playbackStatus = playbackStatus;
        this.title = title;
        this.lenght = lenght;
        this.volume = volume;
        this.currTime = currTime;
        this.readyTimeString = readyTimeString;
        this.type = type;
        this.id = id;
    }

    public Player() {
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getPlaybackStatus() {
        return playbackStatus;
    }

    public void setPlaybackStatus(String playbackStatus) {
        this.playbackStatus = playbackStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public double getLenght() {
        return lenght;
    }

    public void setLenght(double lenght) {
        this.lenght = lenght;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getCurrTime() {
        return currTime;
    }

    public void setCurrTime(double currTime) {
        this.currTime = currTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return name != null ? name.equals(player.name) : player.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String getReadyTimeString() {
        return readyTimeString;
    }

    public void setReadyTimeString(String readyTimeString) {
        this.readyTimeString = readyTimeString;
    }
}
