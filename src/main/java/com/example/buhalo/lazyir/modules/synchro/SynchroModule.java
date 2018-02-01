package com.example.buhalo.lazyir.modules.synchro;

import android.widget.Button;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.CommandsList;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 09.03.17.
 */

public class SynchroModule extends Module {
    private final static String DELETE_COMMANDS = "delete_cmds";
    private final static String ADD_COMMAND = "add_cmd";
    private final static String GET_ALL_COMMANDS = "all_cmd";
    private final static String UPDATE_COMMANDS = "update_cmd";

    private boolean comparing = false;
    private boolean receivingCommands;
    private boolean endReceivingCommands;
    private List<Command> commands = new ArrayList<>();
    @Override
    public  void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data)
        {
            case DELETE_COMMANDS:
                deleteCommands(np);
                break;
            case ADD_COMMAND:
                addCommand(np);
                break;
            case UPDATE_COMMANDS:
                updateCommands(np);
            case GET_ALL_COMMANDS:
                sendAllCommands();
                break;
        }

    }

    @Override
    public void endWork() {
        
    }

    private void sendAllCommands() {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(),GET_ALL_COMMANDS);
        List<Command> commandFull = DBHelper.getInstance(context).getCommandFull();
        CommandsList cmdList = new CommandsList(commandFull);
        np.setObject("cmds",cmdList);
        BackgroundService.sendToDevice(device.getId(),np.getMessage());
    }

    private void addCommand(NetworkPackage np) {
        CommandsList cmds = np.getObject("cmdsA", CommandsList.class);
        if(cmds == null)
            return;
        List<Command> commands = cmds.getCommands();
        if(commands == null || commands.size() < 1)
            return;
        for (Command command : commands) {
            DBHelper.getInstance(context).saveCommand(command);
        }

    }

    private void updateCommands(NetworkPackage np){
        CommandsList cmdsU = np.getObject("cmdsU", CommandsList.class);
        if(cmdsU == null)
            return;
        List<Command> commands = cmdsU.getCommands();
        if(commands == null || commands.size() < 1)
            return;
        List<Button> buttons = DBHelper.getInstance(context).getButtons("1", context); // from first tab
        List<Command> commandList = new ArrayList<>();
        for (Button button : buttons) {
          commandList.addAll(DBHelper.getInstance(context).getBtnCommands(String.valueOf(button.getId())));
        }
        for (Command command : commands) {
            DBHelper.getInstance(context).updateCommand(command);
            for (Command command1 : commandList) {
                if(command.getCommand_name().equals(command1.getCommand_name()))
                    DBHelper.getInstance(context).updateBtnCommand(command1.getOwner_id(),command1);
            }

        }
    }

    private void deleteCommands(NetworkPackage np) {
        CommandsList cmds = np.getObject("cmdsD", CommandsList.class);
        if(cmds == null)
        return;
        List<Command> commands = cmds.getCommands();
        if(commands == null || commands.size() < 1)
            return;
        List<Button> buttons = DBHelper.getInstance(context).getButtons("1", context); // from first tab
        List<Command> commandList = new ArrayList<>();
        for (Button button : buttons) {
            commandList.addAll(DBHelper.getInstance(context).getBtnCommands(String.valueOf(button.getId())));
        }
        for(Command command : commands) {
            DBHelper.getInstance(context).deleteCommand(command);
            for (Command command1 : commandList) {
                if(command.getCommand_name().equals(command1.getCommand_name()))
                    DBHelper.getInstance(context).removeCommandBtn(command1.getOwner_id(),command1.getCommand_name());
            }
        }
    }

    private void compareMethod() {

    }
}
