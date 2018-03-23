package com.example.buhalo.lazyir.modules.reminder;

import com.example.buhalo.lazyir.modules.notificationModule.notifications.Notifications;
import com.example.buhalo.lazyir.modules.notificationModule.sms.SmsPack;

/**
 * Created by buhalo on 26.01.18.
 */

public class MessagesPack {
    private Notifications notifications;
    private SmsPack smsPack;

    public MessagesPack(Notifications notifications, SmsPack smsPack) {
        this.notifications = notifications;
        this.smsPack = smsPack;
    }

    public MessagesPack() {
    }

    public Notifications getNotifications() {
        return notifications;
    }

    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    public SmsPack getSmsPack() {
        return smsPack;
    }

    public void setSmsPack(SmsPack smsPack) {
        this.smsPack = smsPack;
    }
}
