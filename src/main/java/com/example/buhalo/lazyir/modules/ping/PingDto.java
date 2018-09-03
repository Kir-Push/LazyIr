package com.example.buhalo.lazyir.modules.ping;

import com.example.buhalo.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PingDto extends Dto {
    String command;
}
