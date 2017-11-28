package com.example.buhalo.lazyir.modules.notificationModule;

/**
 * Created by buhalo on 26.03.17.
 */

public class Sms {
    private int number;
    private String name;
    private String text;
    private String icon; // todo in server
    private String picture;

    public Sms() {
    }

    public Sms(String name, String text,String icon,String picture) {
        this.name = name;
        this.text = text;
        this.picture = picture;
        this.icon = icon;
    }

    public Sms(int number, String text,String icon,String picture) {
        this.number = number;
        this.text = text;
        this.picture = picture;
        this.icon = icon;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
