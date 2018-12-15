package com.example.buhalo.lazyir.modules.touch;

import com.example.buhalo.lazyir.api.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyboardDto extends Dto {
    private String command;
    private String id;
    private char keycode;
    private String symbol;

    public KeyboardDto(String command, char keycode,String id) {
        this.command = command;
        this.keycode = keycode;
        this.id = id;
    }

    public KeyboardDto(String command, String symbol,String id) {
        this.command = command;
        this.symbol = symbol;
        this.id = id;
    }


}
