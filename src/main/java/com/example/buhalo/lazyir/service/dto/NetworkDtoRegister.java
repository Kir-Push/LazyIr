package com.example.buhalo.lazyir.service.dto;
import com.example.buhalo.lazyir.service.network.tcp.TcpConnectionManager;

public class NetworkDtoRegister {
    public Class getBaseDto(String type){
        if(type.equalsIgnoreCase(TcpConnectionManager.api.TCP.name())){
            return TcpDto.class;
        }
       return null;
   }
}
