package com.example.buhalo.lazyir.modules.synchro;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 09.03.17.
 */
// args in coomand is 1 name 2 command, 3 type
public class SynchroModule extends Module {
    private static String START_RECEIVING = "startReceiving";
    private static String STOP_RECEIVING = "stopReceiving";
    private static String SYNCHRO_STEP = "synchroStep";

    private boolean comparing = false;
    private boolean receivingCommands;
    private boolean endReceivingCommands;
    private List<Command> commands = new ArrayList<>();
    @Override
    public synchronized void execute(NetworkPackage np) { //todo think need there synchronized or no


        if(np.getData().equals(SYNCHRO_STEP) && receivingCommands && !endReceivingCommands)
        {
            List<String> args = np.getArgs();
            commands.add(new Command(args.get(0),args.get(1),device.getId(),args.get(3)));
        } else if(np.getData().equals(START_RECEIVING))
        {
            receivingCommands = true;
        }
        else if(np.getData().equals(STOP_RECEIVING) && receivingCommands)
        {
            endReceivingCommands = true;
            if(!comparing) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            comparing = true;
                            compareMethod();
                        } finally {
                            comparing = false;
                        }
                    }
                }).start();
            }
        }

    }

    private void compareMethod() {
        DBHelper.getInstance(context).removeCommandsAll();
        for(Command command : commands)
        {
            DBHelper.getInstance(context).saveCommand(command);
        }
    }
}
