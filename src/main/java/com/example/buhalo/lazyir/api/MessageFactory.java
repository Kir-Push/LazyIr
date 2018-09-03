package com.example.buhalo.lazyir.api;


import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;

public class MessageFactory {
    private DtoSerializer dtoSerializer;
    private Gson gson;
    private MessageCache cache;
    private String name;
    private String id;
    private String deviceType; //This variable depends on app version(pc or android)

    @Inject
    public MessageFactory(DtoSerializer dtoSerializer) {
        this.dtoSerializer = dtoSerializer;
        init();
    }

    private void init() {
        gson = new GsonBuilder().registerTypeAdapter(Dto.class, dtoSerializer).create();
        name = BackgroundUtil.getMyName();
        deviceType = "android";
        id = BackgroundUtil.getMyId();
        cache = new MessageCache();
        cache.warm();
    }

    public NetworkPackage parseMessage(String msg){
        NetworkPackage np = gson.fromJson(msg,NetworkPackage.class);
        return np;
    }

    private String serialize(NetworkPackage np){
        return gson.toJson(np);
    }

    private NetworkPackage createNetworkPackage(String type,boolean isModule,Dto dto){
        return new NetworkPackage(id,name,deviceType,type,isModule,dto);
    }

    public String createMessage(String type,boolean isModule,Dto dto){
        String msg = getCachedMessage(type);
        return msg != null ? msg : serialize(createNetworkPackage(type, isModule, dto));
    }

    private String getCachedMessage(String type){
        return cache.getCachedMessage(type);
    }

}
