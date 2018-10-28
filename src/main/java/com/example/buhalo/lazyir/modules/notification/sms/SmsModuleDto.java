package com.example.buhalo.lazyir.modules.notification.sms;

import com.example.buhalo.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class SmsModuleDto extends Dto {
    private String command;
    private Sms sms;
}
