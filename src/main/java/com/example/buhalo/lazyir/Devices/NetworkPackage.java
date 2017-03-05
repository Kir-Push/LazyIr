package com.example.buhalo.lazyir.Devices;

import com.example.buhalo.lazyir.Exception.ParseError;
import com.example.buhalo.lazyir.Exception.TcpError;

import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

//package spec's is type::id::name::data::nArgs::arg1::arg2::...
public class NetworkPackage {
    private String type;
    private String id;
    private String name;
    private String data;
    private List<String> args;
    private Device dv;


    public NetworkPackage() {
    }

    public NetworkPackage(String type, String id, String name, String data, List<String> args) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.data = data;
        this.args = args;
    }

    public void parsePackage(String received)
    {
        String trimmed = received.trim();
        String[] array = trimmed.split("::");

       for(int i = 0;i<array.length;i++)
       {
           if(array.length > 5 && i > 4)
           {
               addArg(array[i]);
           }

           switch (i)
           {
               case 0:
                   type = array[i];
                   break;
               case 1:
                   id = array[i];
                   break;
               case 2:
                   name = array[i];
                   break;
               case 3:
                   data = array[i];
                   break;
           }
       }
    }

    public void addArg(String arg)
    {
        args.add(arg);
    }

    public String createMessage() throws ParseError {
        if(type == null || type.isEmpty() || data == null || data.isEmpty())
        {
            throw new ParseError("You have null or empty entries in message + type" + type + " data " + data);
        }
        if(id == null || id.isEmpty() || name == null || name.isEmpty())
        {
            setId(android.provider.Settings.Secure.ANDROID_ID);
            setName(android.os.Build.MODEL);
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(type);
        buffer.append("::");
        buffer.append(id);
        buffer.append("::");
        buffer.append(name);
        buffer.append("::");
        buffer.append(data);
        buffer.append("::");
        if(args != null) {
            buffer.append(args.size());

            for (String arg : args) {
                buffer.append("::");
                buffer.append(arg);
            }
        }
        return buffer.toString();
    }

    public String createFromTypeAndData(String type,String data) throws ParseError
    {
        setType(type);
        setData(data);
        setId(android.provider.Settings.Secure.ANDROID_ID);
        setName(android.os.Build.MODEL);

        return createMessage();
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public Device getDv() {
        return dv;
    }

    public void setDv(Device dv) {
        this.dv = dv;
    }
}
