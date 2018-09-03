package com.example.buhalo.lazyir.modules.memory;

import com.example.buhalo.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoryDto extends Dto {
    private String command;
    private CRTEntity crtEntity;
    private MemoryEntity memoryEntity;

    public MemoryDto(String command, CRTEntity crtEntity) {
        this.command = command;
        this.crtEntity = crtEntity;
    }

    public MemoryDto(String command, MemoryEntity memoryEntity) {
        this.command = command;
        this.memoryEntity = memoryEntity;
    }
}
