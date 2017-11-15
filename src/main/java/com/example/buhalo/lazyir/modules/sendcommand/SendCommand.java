package com.example.buhalo.lazyir.modules.sendcommand;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.CommandsList;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

public class SendCommand extends Module {
    public static final String SEND_COMMAND = "SendCommand";
    public static final String RECEIVED_COMMAND = "receivedCommand";
    public static final String EXECUTE = "execute";

    @Override
    public void execute(final NetworkPackage np) {
        BackgroundService.executorService.submit(new Runnable() {
            @Override
            public void run() {
                if(np.getData().equals(EXECUTE)) {
                    sendCommands(np);
                }
                else if(np.getData().equals(RECEIVED_COMMAND)) {
                    final List<Command> args = np.getObject(NetworkPackage.N_OBJECT, CommandsList.class).getCommands();
                    saveCommand(args);
                }
            }
        });
    }

    @Override
    public void endWork() {

    }

    private void sendCommands(NetworkPackage np)
    {
        String message;
        message = np.getMessage();
        TcpConnectionManager.getInstance().sendCommandToServer(device.getId(),message);
    }

    private void saveCommand(List<Command> args)
    {
        for(Command command : args)
        {
            DBHelper.getInstance(context).saveCommand(command);
        }
        // for save comamnd protocol is data is type command, arg-1 is cmd name, arg-2 cmd, arg-3 type(ir or pc);
    }
}
