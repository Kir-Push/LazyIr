package com.example.buhalo.lazyir.device;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModuleSetting {
    private String name;
    private boolean enabled;
    private List<String> ignoredId;
    private boolean workOnly;
}
