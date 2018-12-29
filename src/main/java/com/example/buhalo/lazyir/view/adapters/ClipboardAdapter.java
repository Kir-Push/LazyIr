package com.example.buhalo.lazyir.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.clipboard.ClipBoard;
import com.example.buhalo.lazyir.modules.clipboard.ClipBoardDto;
import com.example.buhalo.lazyir.modules.clipboard.ClipboardDB;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ClipboardAdapter extends BaseAdapter {

    private LayoutInflater lInflater;
    private DBHelper dbHelper;
    private List<ClipboardDB> clipboard;
    private Context context;

    public ClipboardAdapter(Context context, DBHelper dbHelper, List<ClipboardDB> clipboard) {
        this.lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.dbHelper = dbHelper;
        this.clipboard = clipboard;
        this.context = context;
    }

    @Override
    public int getCount() {
        return clipboard.size();
    }

    @Override
    public Object getItem(int position) {
        return clipboard.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.clipboard_item, parent, false);
        }
        ClipboardDB clipboard = (ClipboardDB) getItem(position);
        TextView clipboardText = (TextView)view.findViewById(R.id.clipboardText);
        clipboardText.setText(clipboard.getText());
        clipboardText.setTag(clipboard.getText());
        clipboardText.setClickable(true);
        clipboardText.setOnClickListener(v ->{
            EventBus.getDefault().post(new ClipBoardDto(ClipBoard.api.RECEIVE.name(), (String) v.getTag()));
            notifyChanged();
        });
        ImageView deleteView = (ImageView) view.findViewById(R.id.delete_clipboard_btn);
        deleteView.setTag(clipboard);
        deleteView.setClickable(true);
        deleteView.setOnClickListener(v->{
            dbHelper.deleteClipboard((ClipboardDB) v.getTag());
            notifyChanged();
        });
        TextView clipboardOwner = (TextView)view.findViewById(R.id.clibpoard_owner);
        clipboardOwner.setText(clipboard.getOwner());

        return view;
    }

    public void notifyChanged(){
        clipboard = dbHelper.getClipboardFull();
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
