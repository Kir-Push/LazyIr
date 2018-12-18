package com.example.buhalo.lazyir.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.sendcommand.Command;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommand;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommandDto;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class CommandsAdapter extends BaseAdapter {

    private LayoutInflater lInflater;
    private DBHelper dbHelper;
    private List<Command> commands;
    private String selectedId;

    public CommandsAdapter(Context context, DBHelper dbHelper, List<Command> commands,String selectedId) {
        this.lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.dbHelper = dbHelper;
        this.commands = commands;
        this.selectedId = selectedId;
    }

    @Override
    public int getCount() {
        return commands.size();
    }

    @Override
    public Object getItem(int position) {
        return commands.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.command_item, parent, false);
        }
        Command command = (Command) getItem(position);
        ((TextView)view.findViewById(R.id.command_name)).setText(command.getCommandName());
        ImageView deleteView = (ImageView) view.findViewById(R.id.delete_image);
        deleteView.setTag(command);
        deleteView.setClickable(true);
        deleteView.setOnClickListener(v->{
            HashSet<Command> cmd = new HashSet<>();
            cmd.add((Command) v.getTag());
            EventBus.getDefault().post(new SendCommandDto(SendCommand.api.DELETE_COMMANDS.name(),selectedId,cmd));
        });
        ImageView runVIew = (ImageView) view.findViewById(R.id.run_image);
        runVIew.setTag(command);
        runVIew.setClickable(true);
        runVIew.setOnClickListener(v->{
            HashSet<Command> cmd = new HashSet<>();
            cmd.add((Command) v.getTag());
            EventBus.getDefault().post(new SendCommandDto(SendCommand.api.EXECUTE.name(),selectedId,cmd));
        });
        ImageView editView = (ImageView) view.findViewById(R.id.edit_image);
        editView.setTag(command);
        editView.setClickable(true);
        editView.setOnClickListener(v->{
          //todo
        });
        return view;
    }
}
