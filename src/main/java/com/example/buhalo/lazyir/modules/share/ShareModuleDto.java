package com.example.buhalo.lazyir.modules.share;

import com.example.buhalo.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ShareModuleDto extends Dto {
    private String command;
    private String userName;
    private String password;
    private String mountPoint;
    private String osType;
    private int port;
    private PathWrapper externalMountPoint;

    public ShareModuleDto(String command) {
        this.command = command;
    }

    public ShareModuleDto(String command, String osType) {
        this.command = command;
        this.osType = osType;
    }
}
