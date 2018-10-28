package com.example.buhalo.lazyir.modules.notification.notifications;

import com.example.buhalo.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ShowNotificationDto extends Dto {
    private String command;
    private List<Notification> notifications;
    private Notification notification;

    public ShowNotificationDto(String command) {
        this.command = command;
    }

    public ShowNotificationDto(String command, Notification notification) {
        this.command = command;
        this.notification = notification;
    }

    public ShowNotificationDto(String command, List<Notification> notifications) {
        this.command = command;
        this.notifications = notifications;
    }
}
