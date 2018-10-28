package com.example.buhalo.lazyir.modules.battery;

import com.example.buhalo.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class BatteryDto extends Dto {
    private String percentage;
    private String status;
}
