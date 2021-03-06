package com.example.buhalo.lazyir.modules.sendcommand;

import com.example.buhalo.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class SendCommandDto extends Dto {
    private String command;
    private String id;
    private Set<Command> commands;

    public SendCommandDto(String command, Set<Command> commands) {
        this.command = command;
        this.commands = commands;
    }


}
