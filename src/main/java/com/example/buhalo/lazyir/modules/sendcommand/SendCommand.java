package com.example.buhalo.lazyir.modules.sendcommand;

import android.content.Context;
import android.widget.Button;

import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.Module;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class SendCommand extends Module {
    public enum api{
        EXECUTE,
        DELETE_COMMANDS,
        ADD_COMMAND,
        GET_ALL_COMMANDS,
        UPDATE_COMMANDS
    }

    private DBHelper dbHelper;

    @Inject
    public SendCommand(MessageFactory messageFactory, Context context, DBHelper dbHelper) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
        this.dbHelper = dbHelper;
    }

    @Override
    public void execute(NetworkPackage np) {
        SendCommandDto dto = (SendCommandDto) np.getData();
        api command = api.valueOf(dto.getCommand());
        switch (command){
            case GET_ALL_COMMANDS:
                sendAllCommands();
                break;
            case UPDATE_COMMANDS:
                updateCommands(dto);
                break;
            case ADD_COMMAND:
                addCommand(dto);
                break;
            case DELETE_COMMANDS:
                deleteCommands(dto);
                 break;
            default:
                break;
        }
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
    }


    private void sendAllCommands() {
        Set<Command> commands = new HashSet<>(dbHelper.getCommandFull());
        String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, new SendCommandDto(api.GET_ALL_COMMANDS.name(), commands));
        sendMsg(message);
    }

    private void updateCommands(SendCommandDto dto){
        Set<Command> commands = dto.getCommands();
        if(commands == null || commands.isEmpty()) {
            return;
        }
        List<Button> buttons = dbHelper.getButtons("1", context); // from first tab
        List<Command> commandList = new ArrayList<>();
        Stream.of(buttons).forEach(btn -> commandList.addAll(dbHelper.getBtnCommands(String.valueOf(btn.getId()))));
        Stream.of(commands).forEach(command -> {
            dbHelper.updateCommand(command);
        Stream.of(commandList).filter(cmd -> cmd.getCommandName().equals(command.getCommandName())).forEach(cmd -> dbHelper.updateBtnCommand(command));
        });
    }

    private void addCommand(SendCommandDto dto) {
        Set<Command> commands = dto.getCommands();
        if(commands == null || commands.isEmpty()) {
            return;
        }
        Stream.of(commands).forEach(dbHelper::saveCommand);
    }

    private void deleteCommands(SendCommandDto dto) {
        Set<Command> commands = dto.getCommands();
        if(commands == null || commands.isEmpty()) {
            return;
        }
        List<Button> buttons = dbHelper.getButtons("1", context); // from first tab
        List<Command> commandList = new ArrayList<>();
        Stream.of(buttons).forEach(btn -> commandList.addAll(dbHelper.getBtnCommands(String.valueOf(btn.getId()))));

        Stream.of(commands).forEach(command -> {
            dbHelper.deleteCommand(command);
            Stream.of(commandList).filter(cmd -> cmd.getCommandName().equals(command.getCommandName())).forEach(cmd -> dbHelper.removeCommandBtn(cmd.getOwnerId(),cmd.getCommandName()));
        });
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void commandFromActivity(SendCommandDto dto){
        if(dto.getId().equals(device.getId()) && dto.getCommand().equals(api.EXECUTE.name())){
            sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,dto));
        }
    }
}
