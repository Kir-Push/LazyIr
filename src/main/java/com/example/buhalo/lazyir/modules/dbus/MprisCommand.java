package com.example.buhalo.lazyir.modules.dbus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MprisCommand {
    private String command;
    private String id;
    private Player player;
    private int data;

    public MprisCommand(String command, String id) {
        this.command = command;
        this.id = id;
    }

    public MprisCommand(String command, String id, Player player) {
        this.command = command;
        this.id = id;
        this.player = player;
    }
}
