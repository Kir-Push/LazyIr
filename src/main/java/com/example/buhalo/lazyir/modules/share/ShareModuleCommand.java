package com.example.buhalo.lazyir.modules.share;

import com.example.buhalo.lazyir.modules.ModuleCmds;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShareModuleCommand {
    private ModuleCmds command;
    private String id;

}
