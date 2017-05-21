package com.example.buhalo.lazyir.UI;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.modules.SendCommand.CommandActivity;
import com.example.buhalo.lazyir.modules.SendIr.IrActivity;
import com.example.buhalo.lazyir.modules.dbus.MediaRemoteActivity;
import com.example.buhalo.lazyir.modules.shareManager.SftpServer;
import com.example.buhalo.lazyir.modules.shareManager.ShareActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static com.example.buhalo.lazyir.Executors.ButtonExecutor.executeButtonCommands;


/**
 * Created by buhalo on 28.01.17.
 */

public class PageFragment extends Fragment implements View.OnTouchListener, View.OnDragListener,View.OnLongClickListener,View.OnClickListener {

    public static final String ARG_PAGE = "ARG_PAGE";

    private static final int ADDCODES = 1;

    private int mPage;

    private  TabLayout tabLayout;

    private  RelativeLayout layout;

    private  ConstraintLayout commandLayout;

    private Set<String> buttonCommandsSet;


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


        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(MainActivity.selected_id);
        View view;
        tabLayout = (TabLayout)  ((Activity) getContext()).findViewById(R.id.sliding_tabs);
        view = inflater.inflate(R.layout.fragment_page, container, false);
        layout = (RelativeLayout) view.findViewById(R.id.fr_pg);
        hideCommandLayout();
        layout.setOnDragListener(this);

        if(mPage == 1)
        {
            createFirstPage();
        }
        else if (mPage == 2)
        {
           return createSecondPage(view,inflater,container);
        }
        else if(mPage == 3)
        {
          return  createThirdPage(view,inflater,container);
        }
        else if(mPage == 4)
        {
            createFourthPage(inflater,container);
        }
        return view;
    }

    private View createFirstPage()
    {
        List<Button> buttonList =   DBHelper.getInstance(getContext().getApplicationContext()).getButtons("1",getContext());
        for(final Button button : buttonList)
        {
            layout.addView(button);
            button.setBackgroundResource(findResourceId(button.getTag()));
            button.setScaleX(1);
            button.setScaleY(1);
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v,
                                    "scaleX", 0.8f);
                            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v,
                                    "scaleY", 0.8f);
                            scaleDownX.setDuration(500);
                            scaleDownY.setDuration(500);

                            AnimatorSet scaleDown = new AnimatorSet();
                            scaleDown.play(scaleDownX).with(scaleDownY);

                            scaleDown.start();

                            break;

                        case MotionEvent.ACTION_UP:
                            ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(
                                    v, "scaleX", 1f);
                            ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(
                                    v, "scaleY", 1f);
                            scaleDownX2.setDuration(500);
                            scaleDownY2.setDuration(500);

                            AnimatorSet scaleDown2 = new AnimatorSet();
                            scaleDown2.play(scaleDownX2).with(scaleDownY2);

                            scaleDown2.start();
                            mainButtonOnclick(v.getId());
                            v.setEnabled(true);
                            break;
                    }
                    return true;
                }
            });
        }
        return null; // return null because first page don't need view
    }

    private View createSecondPage(View vv,LayoutInflater inflater,final ViewGroup container)
    {
        vv = inflater.inflate(R.layout.page_two,container,false);
        vv.findViewById(R.id.share_start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                startActivity(intent);
            }
        });
        vv.findViewById(R.id.media_start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MediaRemoteActivity.class);
                startActivity(intent);
            }
        });
        return vv;
    }

    private View createThirdPage(View vv,LayoutInflater inflater,final ViewGroup container)
    {
        vv = inflater.inflate(R.layout.tab_selector, container, false);
        ViewGroup vv4 = (ViewGroup) vv;
        for(int i=0;i< vv4.getChildCount();i++)
        {

            Button tempBtn = (Button) vv4.getChildAt(i);
            tempBtn.setOnTouchListener(this);
        }
        return vv;
    }

    private View createFourthPage(LayoutInflater inflater,final ViewGroup container)
    {
        List<Button> buttonList =   DBHelper.getInstance(getContext()).getButtons("1",getContext());
        for(Button button : buttonList)
        {
            layout.addView(button);
            button.setBackgroundResource(findResourceId(button.getTag()));
            button.setOnLongClickListener(this);
            button.setOnClickListener(this);
        }
        return null;
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
                Button nButton = new Button(getContext());
                Button receivedButton = (Button)event.getLocalState();
                nButton.setId(receivedButton.getId());
                nButton.setText(receivedButton.getText());
                nButton.setX(event.getX()-receivedButton.getWidth()/2);
                nButton.setY(event.getY()-receivedButton.getHeight()/2);
                nButton.setLayoutParams(new RelativeLayout.LayoutParams(receivedButton.getLayoutParams().width,receivedButton.getLayoutParams().height));
                nButton.setTag(receivedButton.getTag());
                nButton.setBackgroundResource(findResourceId(receivedButton.getTag()));
                ImageButton imgButton = (ImageButton)layout.findViewById(R.id.imageButton4);
                if(nButton.getY()+nButton.getHeight() >= imgButton.getY() && imgButton.getY()>0)
                {

                        DBHelper.getInstance(getContext()).removeButton(String.valueOf(nButton.getId()));
                        layout.removeView(nButton);
                }
                else
                {
                    nButton.setOnTouchListener(null);
                    nButton.setOnLongClickListener(this);
                    nButton.setOnClickListener(this);
                        layout.addView(nButton);
                  if( event.getClipData().getItemAt(0).getText().equals("update"))
                    {
                        DBHelper.getInstance(getContext()).updateButton(nButton,"1");
                    }
                    else
                  {
                      DBHelper.getInstance(getContext()).saveButton(nButton,"1");
                  }
                }
                imgButton.setVisibility(View.INVISIBLE);
                break;
            }
        }
        return true;
    }

    private int findResourceId(Object tag) {
        if(tag == null)
        {
            return -1;
        }
        String path = (String) tag;
        for (Field field : R.mipmap.class.getDeclaredFields()) {
            if(path.endsWith(field.getName()+".png"))
            {
                try {
                    return field.getInt(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
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
        buttonCommandList = (ListView) commandLayout.findViewById(R.id.button_command_list);
        Button add = (Button) commandLayout.findViewById(R.id.const_bt_add);
        Button remove = (Button) commandLayout.findViewById(R.id.const_bt_rmv);
        Button cmd = (Button) commandLayout.findViewById(R.id.const_bt_add_cmd);

        buttonCommandsSet = getCommands(id);
        //commandAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_multiple_choice,new ArrayList<>(commands));
        buttonCommandList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        buttonCommandAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_multiple_choice, new ArrayList<>(buttonCommandsSet));
        buttonCommandList.setAdapter(buttonCommandAdapter);


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), IrActivity.class);
                Bundle b = new Bundle();
                b.putInt("btnId", Integer.parseInt(id)); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivityForResult(intent, ADDCODES);

            }
        });
        cmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CommandActivity.class);
                Bundle b = new Bundle();
                b.putInt("btnId", Integer.parseInt(id)); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivityForResult(intent, ADDCODES);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADDCODES) {
            if (resultCode == RESULT_OK) {
                buttonCommandsSet = getCommands(Integer.toString(data.getIntExtra("btnId",0)));
                buttonCommandAdapter.clear();
                buttonCommandAdapter.addAll(buttonCommandsSet);
                buttonCommandAdapter.notifyDataSetChanged();
            }
        }
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
        List<Command> btnCommands =   DBHelper.getInstance(getContext()).getBtnCommands(id);
        Set<String> set = new HashSet<>();
        for(Command cmd : btnCommands)
        {
            set.add(cmd.getCommand_name());
        }
        return set;
    }

    private void removeCommand(String id,List<String> commands)
    {
        for(String cmd : commands)
        {
            DBHelper.getInstance(getContext()).removeCommandBtn(id,cmd);
        }
    }

    private void mainButtonOnclick(int id)
    {
        executeButtonCommands(getContext(),String.valueOf(id), MainActivity.selected_id);
    }


}
