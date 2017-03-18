package com.example.buhalo.lazyir.modules.SendCommand;

import android.util.Log;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.Exception.ParseError;
import com.example.buhalo.lazyir.Exception.TcpError;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

public class SendCommand extends Module {
    public static final String SEND_COMMAND = "SendCommand";
    public static final String RECEIVED_COMMAND = "receivedCommand";


    @Override
    public void execute(NetworkPackage np) {
        List<String> args = np.getArgs();
        if(np.getData().equals(SEND_COMMAND))
        {
            sendCommands(np);
        }
        else if(np.getData().equals(RECEIVED_COMMAND))
        {
           saveCommand(args);
        }
    }

    private void sendCommands(NetworkPackage np)
    {
        String message;
        try {
            message = np.createMessage();
            TcpConnectionManager.getInstance().sendCommandToServer(device.getId(),message);
        } catch (ParseError | TcpError sendError) {
            Log.e("SendCommand",sendError.toString());
        }
    }

    private void saveCommand(List<String> args)
    {
        if(args.size() < 3)
        {
            return;
        }
        DBHelper.getInstance(context).saveCommand(new Command(args.get(0),args.get(1),device.getId(),args.get(2))); // for save comamnd protocol is data is type command, arg-1 is cmd name, arg-2 cmd, arg-3 type(ir or pc);
    }
}
