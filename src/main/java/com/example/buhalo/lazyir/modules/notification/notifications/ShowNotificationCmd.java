package com.example.buhalo.lazyir.modules.notification.notifications;

import android.view.KeyEvent;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShowNotificationCmd {
    private String cmd;
    private String id;
    private ShowNotificationDto dto;

    public ShowNotificationCmd(String cmd, ShowNotificationDto dto) {
        this.cmd = cmd;
        this.dto = dto;
    }
}
