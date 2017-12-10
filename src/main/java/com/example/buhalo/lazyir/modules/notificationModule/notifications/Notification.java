package com.example.buhalo.lazyir.modules.notificationModule.notifications;

/**
 * Created by buhalo on 26.03.17.
 */

public class Notification {

    private String text;
    private String title;
    private String pack;
    private String ticker;
    private String id;
    private String type;
    private String icon;
    private String picture;

    public Notification() {
    }

    public Notification(String text,String type, String title, String pack, String ticker, String id,String icon,String picture) {
        this.text = text;
        this.type = type;
        this.title = title;
        this.pack = pack;
        this.ticker = ticker;
        this.id = id;
        this.icon = icon;  // if null, server will use standart's icon
        this.picture = picture; // if null, don't show
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
