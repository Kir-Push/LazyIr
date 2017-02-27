package com.example.buhalo.lazyir.UI;

import android.app.Activity;
import android.content.ClipData;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.buhalo.lazyir.DbClasses.DragableButton;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by buhalo on 28.01.17.
 */

public class PageFragment extends Fragment implements View.OnTouchListener, View.OnDragListener,View.OnLongClickListener,View.OnClickListener {

    public static final String ARG_PAGE = "ARG_PAGE";

    public static List<Button> buttons = new ArrayList<>();

    public static Map<Integer,Button> buttonMap = new HashMap<>();

    private int mPage;

    private  TabLayout tabLayout;

    private Activity activity;

    private View rootView;

    private  RelativeLayout layout;

    private Button tempBtn;

    private  ConstraintLayout commandLayout;

    private static String textt;

    private static TabLayout.Tab removedTab;

    private static Set<String> buttonCommandsSet;

    private ListView commandList;

    private ListView buttonCommandList;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        View viewTouch = null;
        rootView = inflater.inflate(R.layout.activity_main,container,false);
        activity = (Activity) rootView.getContext();
        tabLayout = (TabLayout) activity.findViewById(R.id.sliding_tabs);
        viewTouch = inflater.inflate(R.layout.content_main, container, false);
        view = inflater.inflate(R.layout.fragment_page, container, false);
        layout = (RelativeLayout) view.findViewById(R.id.fr_pg);
        hideCommandLayout();
        layout.setOnDragListener(this);

        if(mPage == 1)
        {
            DragableButton dragableButton = new DragableButton();
            DragableButton.DbHelper dragB = dragableButton.new DbHelper(getContext());
            List<Button> buttonList = dragB.getButtons("1",activity);
            for(Button button : buttonList)
            {
                layout.addView(button);
            }
        }
        else if(mPage == 3)
        {
            view = inflater.inflate(R.layout.tab_selector, container, false);
            ViewGroup vv4 = (ViewGroup) view;
            for(int i=0;i< vv4.getChildCount();i++)
            {
                tempBtn = (Button)vv4.getChildAt(i);
                tempBtn.setOnTouchListener(this);
            }

        }
        else if(mPage == 4)
        {
            DragableButton dragableButton = new DragableButton();
            DragableButton.DbHelper dragB = dragableButton.new DbHelper(getContext());
            List<Button> buttonList = dragB.getButtons("1",activity);
            for(Button button : buttonList)
            {
                layout.addView(button);
                button.setOnLongClickListener(this);
                button.setOnClickListener(this);
            }
        }
        else if (mPage == 2)
        {
         //   view = inflater.inflate(R.layout.tab_selector, container, false);
        }

        return view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

            if(event.getAction() == MotionEvent.ACTION_DOWN)
            {

                if(mPage == 3)
                {
                    tabLayout.getTabAt(3).select();
                    ClipData data = ClipData.newPlainText("tab", "4");
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                    v.startDrag(data, shadow, v, 0);
                }

            }


            return false;
        }


    @Override
    public boolean onDrag(View v, DragEvent event) {
        int action = event.getAction();
        switch (action)
        {
            case DragEvent.ACTION_DROP:
            {
                Button nButton = new Button(activity);
                Button receivedButton = (Button)event.getLocalState();
                nButton.setId(receivedButton.getId());
                nButton.setText(receivedButton.getText());
                nButton.setX(event.getX()-receivedButton.getWidth()/2);
                nButton.setY(event.getY()-receivedButton.getHeight()/2);
                nButton.setWidth(receivedButton.getWidth());
                nButton.setHeight(receivedButton.getHeight());
                ImageButton imgButton = (ImageButton)layout.findViewById(R.id.imageButton4);
                if(nButton.getY()+nButton.getHeight() >= imgButton.getY() && imgButton.getY()>0)
                {

                        DragableButton dragableButton = new DragableButton();
                        DragableButton.DbHelper dragB = dragableButton.new DbHelper(getContext());
                        dragB.removeButton(String.valueOf(nButton.getId()));  //TODO sdelatj uldaenie button commands vsmeste s button!!
                        layout.removeView(nButton);
                }
                else
                {
                    nButton.setOnTouchListener(null);
                    nButton.setOnLongClickListener(this);
                    nButton.setOnClickListener(this);
                        layout.addView(nButton);
                        DragableButton dragableButton = new DragableButton();
                        DragableButton.DbHelper dragB = dragableButton.new DbHelper(getContext());
                  if( event.getClipData().getItemAt(0).getText().equals("update"))
                    {
                        dragB.updateButton(nButton,"1");
                    }
                    else
                  {
                      dragB.saveButton(nButton,"1");
                  }
                }
                imgButton.setVisibility(View.INVISIBLE);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        tabLayout.getTabAt(3).select();
        hideCommandLayout();
        unhideButtons();
        ClipData data = ClipData.newPlainText("mode", "update");
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
        v.startDrag(data, shadow, v, 0);
        layout.removeView(v);
        ImageButton imgButton = (ImageButton) layout.findViewById(R.id.imageButton4);
        imgButton.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public void onClick(View v) {

        if(commandLayout != null)
        {
            if(commandLayout.getVisibility() != View.VISIBLE)
            {
                commandLayout.setVisibility(View.VISIBLE);
                commandLayout.bringToFront(); // TODO test only
              hideButtons();
                fillCommandLayout(String.valueOf(v.getId()));
            }
            else
            {
                commandLayout.setVisibility(View.INVISIBLE);
                unhideButtons();
            }


        }
    }


    private ArrayAdapter<String> commandAdapter;
    private ArrayAdapter<String> buttonCommandAdapter;

    private void fillCommandLayout(final String id)
    {
        commandList = (ListView) commandLayout.findViewById(R.id.all_comand_list);
        buttonCommandList = (ListView) commandLayout.findViewById(R.id.button_command_list);
        Button add = (Button) commandLayout.findViewById(R.id.const_bt_add);
        Button remove = (Button) commandLayout.findViewById(R.id.const_bt_rmv);

        Set<String> commands = new HashSet<>();
        buttonCommandsSet = getCommands(id);

        DragableButton dragableButton = new DragableButton();
        DragableButton.DbHelper dragB = dragableButton.new DbHelper(activity); // only for test
        List<Command> command = dragB.getCommand(null);
        for(Command command1 : command)
        {
            commands.add(command1.getCommand_name());
        }

        commandList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        commandAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_multiple_choice,new ArrayList<>(commands));
        commandList.setAdapter(commandAdapter);

        buttonCommandList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        buttonCommandAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_multiple_choice, new ArrayList<>(buttonCommandsSet));
        buttonCommandList.setAdapter(buttonCommandAdapter);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checkedItemPositions = commandList.getCheckedItemPositions();
                for(int i =0;i<checkedItemPositions.size();i++)
                {
                    int key = checkedItemPositions.keyAt(i);
                    if(checkedItemPositions.get(key))
                    {
                        buttonCommandsSet.add(commandAdapter.getItem(key));
                        commandList.setItemChecked(i,false);
                    }
                }
                buttonCommandAdapter.clear();
                buttonCommandAdapter.addAll(buttonCommandsSet);
                buttonCommandAdapter.notifyDataSetChanged();
                addCommand(id,buttonCommandsSet);
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checkedItemPositions = buttonCommandList.getCheckedItemPositions();
                ArrayList<String> removeObj = new ArrayList<String>();
                for(int i =0;i<checkedItemPositions.size();i++)
                {
                    int key = checkedItemPositions.keyAt(i);
                    if(checkedItemPositions.get(key))
                    {
                        removeObj.add(buttonCommandAdapter.getItem(key));
                        buttonCommandList.setItemChecked(i,false);
                        buttonCommandsSet.remove(buttonCommandAdapter.getItem(key));
                    }
                }
                for(String obj : removeObj)
                {
                    buttonCommandAdapter.remove(obj);
                }
                buttonCommandAdapter.notifyDataSetChanged();
                removeCommand(id,removeObj);
            }
        });
    }
    private void hideButtons()
    {
        for(int i =0;i<layout.getChildCount();i++)
        {
            View vv = layout.getChildAt(i);
            if(vv.getId() != commandLayout.getId() && vv.getId() != R.id.imageButton4)
            {
                if(vv.getY() + vv.getHeight() >= commandLayout.getY())
                {
                    vv.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void unhideButtons()
    {
        for(int i =0;i<layout.getChildCount();i++)
        {
            View vv = layout.getChildAt(i);
            if(vv.getId() != commandLayout.getId() && vv.getId() != R.id.imageButton4)
            {
                if(vv.getY() + vv.getHeight() >= commandLayout.getY())
                    vv.setVisibility(View.VISIBLE);
            }
        }
    }


    private void hideCommandLayout()
    {
        commandLayout = (ConstraintLayout) layout.findViewById(R.id.command_set_layout);
        if(commandLayout != null)
        {
            commandLayout.findViewById(R.id.cnts_btn_ok).setOnClickListener(this);
            commandLayout.setVisibility(View.INVISIBLE);
        }
    }

    private Set<String> getCommands(String id)
    {
        DragableButton dragableButton = new DragableButton();
        DragableButton.DbHelper dragB = dragableButton.new DbHelper(activity);
        List<Command> btnCommands = dragB.getBtnCommands(id);
        Set<String> set = new HashSet<>();
        for(Command cmd : btnCommands)
        {
            set.add(cmd.getCommand_name());
        }
        return set;
    }

    private void addCommand(String id,Set<String> commands) {
        DragableButton dragableButton = new DragableButton();
        DragableButton.DbHelper dragB = dragableButton.new DbHelper(activity);
        for(String cmd : commands)
        {
            dragB.saveBtnCommand(cmd,id);
        }
    }

    private void removeCommand(String id,List<String> commands)
    {
        DragableButton dragableButton = new DragableButton();
        DragableButton.DbHelper dragB = dragableButton.new DbHelper(activity);
        for(String cmd : commands)
        {
            dragB.removeCommandBtn(id,cmd);
        }
    }
}
