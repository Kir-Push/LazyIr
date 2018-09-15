package com.example.buhalo.lazyir.modules.notification.messengers;

import com.example.buhalo.lazyir.api.Dto;
import com.example.buhalo.lazyir.modules.notification.notifications.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessengersDto extends Dto {
    private String command;
    private String text;
    private String typeName;
    private Notification notification;

    public MessengersDto(String command, String typeName) {
        this.command = command;
        this.typeName = typeName;
    }

    public MessengersDto(String command) {
        this.command = command;
    }

    public MessengersDto(Notification notification) {
        this.notification = notification;
    }
}
