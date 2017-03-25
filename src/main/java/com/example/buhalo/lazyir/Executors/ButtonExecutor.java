package com.example.buhalo.lazyir.Executors;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.ModuleExecutor;
import com.example.buhalo.lazyir.modules.SendCommand.SendCommand;
import com.example.buhalo.lazyir.modules.SendIr.SendIr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 28.02.17.
 */

public class ButtonExecutor {

    public synchronized static void executeButtonCommands(Context context, String id,String dvId)
    {
        List<Command> btnCommands = DBHelper.getInstance(context).getBtnCommands(id);
        List<String> pc = new ArrayList<>();
        List<String> ir = new ArrayList<>();
        for(Command cmd : btnCommands)
        {
            if(cmd.getType().equals("pc"))
                pc.add(cmd.getCommand_name()); //send command on server and here determine which execute diferrence by os
            else if(cmd.getType().equals("ir"))
                ir.add(cmd.getCommand());
        }
        NetworkPackage npPc = new NetworkPackage();
        npPc.setType(SendCommand.class.getSimpleName());
        npPc.setData(SendCommand.EXECUTE);
        npPc.setArgs(pc);
        npPc.setDv(Device.connectedDevices.get(dvId));
        NetworkPackage npIr = new NetworkPackage();
        npIr.setType(SendIr.class.getSimpleName());
        npIr.setData(SendIr.SEND_IR_COMMAND);
        npIr.setArgs(ir);
        npIr.setDv(Device.connectedDevices.get(dvId));
        Log.d("ButtonExecutor","Check For conenction");
        if(Device.getConnectedDevices().get(dvId) == null)
        {
            Log.d("ButtonExecutor","No connection");
            Toast.makeText(context,"No connection",Toast.LENGTH_SHORT).show();
            ModuleExecutor.executePackageIrOffline(npIr,context);
        }
        else
        {
            ModuleExecutor.executePackage(npPc);
            ModuleExecutor.executePackage(npIr);
        }
        //todo zdesj kakto krivo potom podumaj kak uluchitj, nigde bolwe tak ne delaj
    }
}
