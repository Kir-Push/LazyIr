package com.example.buhalo.lazyir.api;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public abstract class Dto {
   @Getter @Setter private String className;
   @Getter @Setter private boolean isModule;
}
