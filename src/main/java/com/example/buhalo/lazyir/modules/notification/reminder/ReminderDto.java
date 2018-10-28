package com.example.buhalo.lazyir.modules.notification.reminder;

import com.example.buhalo.lazyir.api.Dto;
import com.example.buhalo.lazyir.modules.notification.notifications.Notification;
import com.example.buhalo.lazyir.modules.notification.sms.Sms;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ReminderDto extends Dto {
    private String command;
    private String missedCallStr;
    private List<MissedCall> missedCalls;
    private List<Notification> notifications;
    private List<Sms> smsList;

    public ReminderDto(String command) {
        this.command = command;
    }

    public ReminderDto(String command, String missedCallStr) {
        this.command = command;
        this.missedCallStr = missedCallStr;
    }

    public ReminderDto(String command, List<MissedCall> missedCalls) {
        this.command = command;
        this.missedCalls = missedCalls;
    }

    public ReminderDto(String command, List<Notification> notifications, List<Sms> smsList) {
        this.command = command;
        this.notifications = notifications;
        this.smsList = smsList;
    }
}
