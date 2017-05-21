package com.example.buhalo.lazyir.modules.shareManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.buhalo.lazyir.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 07.03.17.
 */

public class FolderAdater extends BaseAdapter{

  //  Context ctx;
    LayoutInflater lInflater;
    List<FileWrap> fileWrapList;
    private boolean cbSendVisible;

    public FolderAdater(Context ctx, List<FileWrap> fileWrapList) {
     //   this.ctx = ctx;
        this.fileWrapList = fileWrapList;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return fileWrapList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileWrapList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.share_item, parent, false);
        }
        FileWrap fileWrap = getFile(position);
        ((TextView)view.findViewById(R.id.share_fldr_name)).setText(fileWrap.getPath());
        ((ImageView)view.findViewById(R.id.ivImage)).setImageResource(fileWrap.getRes_id());
        CheckBox cbSend = (CheckBox) view.findViewById(R.id.share_ckBox);
        cbSend.setOnCheckedChangeListener(myCheckChangeList);
        cbSend.setTag(position);
        cbSend.setChecked(fileWrap.isChecked());
        if(cbSendVisible && position != 0)
        {
            cbSend.setVisibility(View.VISIBLE);
        }
        else {
            cbSend.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    public FileWrap getFile(int position)
    {
        return fileWrapList.get(position);
    }

    public List<FileWrap> getChecked()
    {
        ArrayList<FileWrap> list = new ArrayList<>();
        for (FileWrap fileWrap : fileWrapList) {
            if(fileWrap.isChecked())
            list.add(fileWrap);
        }
        return list;
    }

    CompoundButton.OnCheckedChangeListener myCheckChangeList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // меняем данные товара (в корзине или нет)
            getFile((Integer) buttonView.getTag()).setChecked(isChecked);
        }
    };

    public void clear()
    {
        fileWrapList.clear();
    }

    public void setFileWrapList(List<FileWrap> list)
    {
        this.fileWrapList = list;
    }

    public void setCbSend(boolean on)
    {
        cbSendVisible = on;
    }


}
