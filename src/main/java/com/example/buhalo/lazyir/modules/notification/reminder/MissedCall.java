package com.example.buhalo.lazyir.modules.notification.reminder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MissedCall {
    private String number;
    private String name;
    private int count;
    private long date;
    private String picture;
    private String id;
}
