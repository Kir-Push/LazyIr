package com.example.buhalo.lazyir.modules.SendIr;

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
    public void execute(NetworkPackage np) {
        if(np.getData().equals(SEND_IR_COMMAND))
        {
           transmitIr(np.getArgs());
        }
    }

    private void transmitIr(List<String> args)
    {
        if(args.size() < 1)
        {
            return;
        }
        IrMethods.processCommands(context,args);
    }
}
