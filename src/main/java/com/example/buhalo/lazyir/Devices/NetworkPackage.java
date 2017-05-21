package com.example.buhalo.lazyir.Devices;

import android.provider.Settings;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

//package spec's is type::id::name::data::nArgs::arg1::arg2::...
public class NetworkPackage {
    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String TYPE = "type";
    public final static String DATA = "data";
    public final static String N_OBJECT = "object";


    private Device dv;

    private String msg;

    JsonNodeFactory factory;
    private ObjectNode idNode;

    public ObjectNode getIdNode() {
        return idNode;
    }

    public void setIdNode(ObjectNode idNode) {
        this.idNode = idNode;
    }

    public NetworkPackage(String type, String data) {
        // args = new ArrayList<>();
        factory = JsonNodeFactory.instance;
        idNode = factory.objectNode();
        idNode.put(ID,getMyId());
        idNode.put(NAME,getMyName());
        idNode.put(TYPE,type);
        idNode.put(DATA,data);
    }

    public NetworkPackage(String message)
    {
        this.msg = message;
        parseMessage();
    }

    public void parseMessage()
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            idNode = (ObjectNode) mapper.readTree(msg);
        } catch (IOException e) {
            Log.e("NetworkPackage",e.toString());
        }
    }

    public void addStringArray(String key,List<String> list)
    {
        ArrayNode arrayNode = idNode.putArray(key);
        for(String st : list)
        {
            arrayNode.add(st);
        }
    }

    public String getMessage()
    {
        return idNode.toString();
    }

    public List<String> getStringArray(String key)
    {
        List<String> list = new ArrayList<>();
        if(idNode.get(key).isArray())
        {
            for(JsonNode objNode: idNode.get(key))
            {
                if(objNode.isTextual())
                    list.add(objNode.asText());
            }
        }
        return list;
    }

    public String getType()
    {
        return getValue(TYPE);
    }

    public String getData()
    {
        return getValue(DATA);
    }

    public String getValue(String key)
    {
        return idNode.get(key).textValue();
    }

    public void setValue(String key,String value)
    {
        idNode.put(key,value);
    }

    public <T> void setObject(String key,T object)
    {
        JsonNode node = new ObjectMapper().convertValue(object, JsonNode.class);
        idNode.set(key,node);
    }

    public <T> T getObject(String key,Class<T> tClass)
    {
        String typeName = idNode.get(TYPE).textValue();
        JsonNode object = idNode.get(key);


        try {
            return new ObjectMapper().readValue(object.toString(),tClass);
        } catch (Exception e) {
            Log.e("NetworkPackage",e.toString());
        }
        return null;
    }

    public String getMyId()
    {
       return android.os.Build.SERIAL;
    }

    public String getMyName()
    {
        return android.os.Build.MODEL;
    }

    public String getId()
    {
        return idNode.get(ID).textValue();
    }

    public String getName()
    {
        return idNode.get(NAME).textValue();
    }


    public Device getDv() {
        return dv;
    }

    public void setDv(Device dv) {
        this.dv = dv;
    }

}

