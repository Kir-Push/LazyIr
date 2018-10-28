package com.example.buhalo.lazyir.modules.notification.call;

import com.example.buhalo.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CallModuleDto extends Dto {
    private String command;
    private String callType;
    private String text;
    private String name;
    private String number;
    private String icon;

    CallModuleDto(String command) {
        this.command = command;
    }

    CallModuleDto(String command, String number) {
        this.command = command;
        this.number = number;
    }

    public CallModuleDto(String command, String callType, String number) {
        this.command = command;
        this.callType = callType;
        this.number = number;
    }
}
