package com.example.buhalo.lazyir.modules.notification.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationListenerCmd {
    private ShowNotification.api command;
    private String notificationId;
    private String senderId;

    public NotificationListenerCmd(ShowNotification.api command,String senderId) {
        this.command = command;
        this.senderId = senderId;
    }




}
