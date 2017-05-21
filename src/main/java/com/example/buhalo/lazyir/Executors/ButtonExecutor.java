package com.example.buhalo.lazyir.Executors;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.CommandsList;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.ModuleExecutor;
import com.example.buhalo.lazyir.modules.SendCommand.SendCommand;
import com.example.buhalo.lazyir.modules.SendIr.SendIr;

import java.util.ArrayList;
import java.util.List;

import static com.example.buhalo.lazyir.modules.SendCommand.SendCommand.EXECUTE;
import static com.example.buhalo.lazyir.modules.SendCommand.SendCommand.SEND_COMMAND;
import static com.example.buhalo.lazyir.modules.SendIr.SendIr.SEND_IR_COMMAND;

/**
 * Created by buhalo on 28.02.17.
 */

public class ButtonExecutor {

    public synchronized static void executeButtonCommands(Context context, String id,String dvId)
    {
        List<Command> btnCommands = DBHelper.getInstance(context).getBtnCommands(id);
        List<Command> pc = new ArrayList<>();
        List<Command> ir = new ArrayList<>();
        for(Command cmd : btnCommands)
        {
            if(cmd.getType().equals("pc"))
                pc.add(cmd); //send command on server and here determine which execute diferrence by os
            else if(cmd.getType().equals("ir"))
                ir.add(cmd);
        }
        CommandsList commandsList = new CommandsList(pc);
        NetworkPackage npPc = new NetworkPackage(SEND_COMMAND,EXECUTE);
      npPc.setObject(NetworkPackage.N_OBJECT,commandsList);
        npPc.setDv(Device.connectedDevices.get(dvId));
        NetworkPackage npIr = new NetworkPackage(SendIr.class.getSimpleName(),SEND_IR_COMMAND);
        CommandsList irCommandsList = new CommandsList(ir);
        npIr.setObject(NetworkPackage.N_OBJECT,irCommandsList);
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
    }
}
