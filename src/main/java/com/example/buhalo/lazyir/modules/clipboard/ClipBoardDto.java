package com.example.buhalo.lazyir.modules.clipboard;

import com.example.buhalo.lazyir.api.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ClipBoardDto extends Dto {
    private String command;
    private String text;
}
