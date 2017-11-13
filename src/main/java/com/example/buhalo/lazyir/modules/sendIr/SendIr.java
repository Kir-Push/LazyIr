package com.example.buhalo.lazyir.modules.sendIr;

import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.CommandsList;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.old.IrMethods;

import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

public class SendIr extends Module {
    public static final String SEND_IR_COMMAND = "sendIrCommand";

    @Override
    public void execute(final NetworkPackage np) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(np.getData().equals(SEND_IR_COMMAND))
                {
                    transmitIr(np.getObject(NetworkPackage.N_OBJECT, CommandsList.class).getCommands());
                }
            }
        }).start();
    }

    @Override
    public void endWork() {

    }

    private void transmitIr(List<Command> args)
    {
        if(args.size() < 1)
        {
            return;
        }
        IrMethods.processCommands(context,args);
    }
}
