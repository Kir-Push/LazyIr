package com.example.buhalo.lazyir.service.dto;

import com.example.buhalo.lazyir.api.Dto;
import com.example.buhalo.lazyir.device.ModuleSetting;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
public class TcpDto extends Dto {
    private String command;
    private String data;
    private String result;
    private String icon;
    private List<ModuleSetting> moduleSettings;

    public TcpDto(String command, String data) {
        this.command = command;
        this.data = data;
    }

    public TcpDto(String command) {
        this.command = command;
    }

    public TcpDto(String command, String data, String result) {
        this.command = command;
        this.data = data;
        this.result = result;
    }
}
