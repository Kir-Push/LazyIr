package com.example.buhalo.lazyir.modules.notificationModule.sms;

import java.util.List;

/**
 * Created by buhalo on 18.01.18.
 */

public class SmsPack { // todo in server
    private List<Sms> sms;

    public SmsPack(List<Sms> sms) {
        this.sms = sms;
    }

    public SmsPack() {
    }

    public List<Sms> getSms() {
        return sms;
    }

    public void setSms(List<Sms> sms) {
        this.sms = sms;
    }
}
