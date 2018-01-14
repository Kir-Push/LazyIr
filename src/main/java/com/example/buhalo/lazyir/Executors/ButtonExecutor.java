package com.example.buhalo.lazyir.Executors;

import android.content.Context;
import android.widget.Toast;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.CommandsList;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.ModuleExecutor;
import com.example.buhalo.lazyir.modules.sendIr.IrModule;

import java.util.ArrayList;
import java.util.List;

import static com.example.buhalo.lazyir.modules.sendcommand.SendCommand.EXECUTE;
import static com.example.buhalo.lazyir.modules.sendcommand.SendCommand.SEND_COMMAND;
import static com.example.buhalo.lazyir.modules.sendIr.IrModule.SEND_IR_COMMAND;


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

        NetworkPackage npPc = NetworkPackage.Cacher.getOrCreatePackage(SEND_COMMAND,EXECUTE);
        npPc.setObject(NetworkPackage.N_OBJECT,commandsList);
        Device device = Device.getConnectedDevices().get(dvId);
        npPc.setDvId(dvId);

        NetworkPackage npIr = NetworkPackage.Cacher.getOrCreatePackage(IrModule.class.getSimpleName(),SEND_IR_COMMAND);
        CommandsList irCommandsList = new CommandsList(ir);
        npIr.setObject(NetworkPackage.N_OBJECT,irCommandsList);
        npIr.setDvId(dvId);

        if(device == null)
        {
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
