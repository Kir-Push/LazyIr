package com.example.buhalo.lazyir.modules.notificationModule;

/**
 * Created by buhalo on 26.03.17.
 */

public class Notification {

    private String text;
    private String title;
    private String pack;
    private String ticker;
    private String id;

    public Notification() {
    }

    public Notification(String text, String title, String pack, String ticker, String id) {
        this.text = text;
        this.title = title;
        this.pack = pack;
        this.ticker = ticker;
        this.id = id;
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
}
