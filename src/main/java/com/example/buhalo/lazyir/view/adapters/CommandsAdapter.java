package com.example.buhalo.lazyir.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.sendcommand.Command;

import java.util.List;

public class CommandsAdapter extends BaseAdapter {

    private LayoutInflater lInflater;
    private DBHelper dbHelper;
    private List<Command> commands;

    public CommandsAdapter(Context context, DBHelper dbHelper, List<Command> commands) {
        this.lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.dbHelper = dbHelper;
        this.commands = commands;
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
        return view;
    }
}
