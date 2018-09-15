package com.example.buhalo.lazyir.api;

import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.service.dto.NetworkDtoRegister;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import javax.inject.Inject;

public class DtoSerializer implements JsonDeserializer<Dto>,JsonSerializer<Dto> {
    private ModuleFactory moduleFactory;
    private NetworkDtoRegister ndtoRegister;

    @Inject
    public DtoSerializer(ModuleFactory moduleFactory,NetworkDtoRegister ndtoRegister) {
        this.moduleFactory = moduleFactory;
        this.ndtoRegister = ndtoRegister;
    }

    @Override
    public Dto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("className").getAsString();
        boolean isModule = jsonObject.get("isModule").getAsBoolean();
        if(isModule) {
            return context.deserialize(json, moduleFactory.getModuleDto(type));
        }else{
            return context.deserialize(json,ndtoRegister.getBaseDto(type));
        }
    }

    @Override
    public JsonElement serialize(Dto src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src);
    }
}
