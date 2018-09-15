package com.example.buhalo.lazyir.bus.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MainActivityCommand {
    private Enum command;
    private String id;
}
