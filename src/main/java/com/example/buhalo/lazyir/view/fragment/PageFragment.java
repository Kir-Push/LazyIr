package com.example.buhalo.lazyir.view.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.sendcommand.Command;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommand;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommandDto;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.view.activity.MainActivity;
import com.example.buhalo.lazyir.view.activity.CommandActivity;
import com.example.buhalo.lazyir.view.activity.MediaRemoteActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.view.activity.TouchActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;

import static android.app.Activity.RESULT_OK;


public class PageFragment extends Fragment implements View.OnTouchListener, View.OnDragListener,View.OnLongClickListener,View.OnClickListener {
    private static final String TAG = "PageFragment";

    public static final String ARG_PAGE = "ARG_PAGE";
    private static final int ADDCODES = 1;
    private int mPage;
    private  TabLayout tabLayout;
    private  RelativeLayout layout;
    private  ConstraintLayout commandLayout;
    private Set<String> buttonCommandsSet;
    @Inject @Getter @Setter
    DBHelper dbHelper;

    @Override
    public void onAttach(Context context) {
        AndroidInjection.inject(this);
        super.onAttach(context);
    }

    // use when non edit in first page
    public final View.OnTouchListener nonEditListenerTouch = (v, event) -> {
        int i = event.getAction();
        if (i == MotionEvent.ACTION_DOWN) {
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 0.8f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 0.8f);
            scaleDownX.setDuration(500);
            scaleDownY.setDuration(500);
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.play(scaleDownX).with(scaleDownY);
            scaleDown.start();
        } else if (i == MotionEvent.ACTION_UP) {
            ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(v, "scaleX", 1f);
            ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(v, "scaleY", 1f);
            scaleDownX2.setDuration(500);
            scaleDownY2.setDuration(500);
            AnimatorSet scaleDown2 = new AnimatorSet();
            scaleDown2.play(scaleDownX2).with(scaleDownY2);
            scaleDown2.start();
            PageFragment.this.mainButtonOnclick(v.getId());
            v.setEnabled(true);
            v.performClick();
        }
        return true;
    };


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
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(supportActionBar != null) {
            supportActionBar.setTitle(BackgroundUtil.getSelectedId());
        }
        tabLayout =((Activity) getContext()).findViewById(R.id.sliding_tabs);
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        layout = view.findViewById(R.id.fr_pg);
        hideCommandLayout();
        layout.setOnDragListener(this);

        if(mPage == 1) {
            if(MainActivity.isEditMode()) {
                createFourthPage(); // still first page, but edit mode on.
            } else {
                createFirstPage();
            }
        } else if (mPage == 2) {
           return createSecondPage(inflater,container);
        } else if(mPage == 3) {
          return  createThirdPage(inflater,container);
        }
        return view;
    }

    public void createFirstPage() {
        List<Button> buttonList =   dbHelper.getButtons("1",getContext());
        for(final Button button : buttonList) {
            layout.addView(button);
            button.setBackgroundResource(findResourceId(button.getTag()));
            button.setScaleX(1);
            button.setScaleY(1);
            button.setOnTouchListener(nonEditListenerTouch);
        }
    }

    private View createSecondPage(LayoutInflater inflater, final ViewGroup container) {
        View vv = inflater.inflate(R.layout.page_two, container, false);
        vv.findViewById(R.id.media_start_btn).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MediaRemoteActivity.class);
            startActivity(intent);
        });
        vv.findViewById(R.id.touch_control).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TouchActivity.class);
            startActivity(intent);
        });
        return vv;
    }

    private View createThirdPage(LayoutInflater inflater, final ViewGroup container) {
        View vv = inflater.inflate(R.layout.tab_selector, container, false);
        ViewGroup vv4 = (ViewGroup) vv;
        for(int i=0;i< vv4.getChildCount();i++) {
            Button tempBtn = (Button) vv4.getChildAt(i);
            tempBtn.setOnTouchListener(this);
        }
        return vv;
    }

    private void createFourthPage(){ // fourth page s basicaly first page with edit mode on!
        List<Button> buttonList =  dbHelper.getButtons("1",getContext());
        for(Button button : buttonList) {
            layout.addView(button);
            button.setBackgroundResource(findResourceId(button.getTag()));
            button.setOnLongClickListener(this);
            button.setOnClickListener(this);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                if(mPage == 3) {
                    TabLayout.Tab tabAt = tabLayout.getTabAt(0);
                    if(tabAt != null) {
                        tabAt.select();
                    }
                    ClipData data = ClipData.newPlainText("tab", "4");
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                    v.startDrag(data, shadow, v, 0);
                }
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                v.performClick();
            }
            return false;
        }


    @Override
    public boolean onDrag(View v, DragEvent event) {
        int action = event.getAction();
        if (action == DragEvent.ACTION_DROP) {
            Button nButton = new Button(getContext());
            Button receivedButton = (Button) event.getLocalState();
            nButton.setId(receivedButton.getId());
            nButton.setText(receivedButton.getText());
            nButton.setX(event.getX() - receivedButton.getWidth() / 2);
            nButton.setY(event.getY() - receivedButton.getHeight() / 2);
            nButton.setLayoutParams(new RelativeLayout.LayoutParams(receivedButton.getLayoutParams().width, receivedButton.getLayoutParams().height));
            nButton.setTag(receivedButton.getTag());
            nButton.setBackgroundResource(findResourceId(receivedButton.getTag()));
            ImageButton imgButton = layout.findViewById(R.id.imageButton4);
            if (nButton.getY() + nButton.getHeight() >= imgButton.getY() && imgButton.getY() > 0) {
                dbHelper.removeButton(String.valueOf(nButton.getId()));
                layout.removeView(nButton);
            } else {
                if (!MainActivity.isEditMode())   // if edit mode off set basic touch listener
                    nButton.setOnTouchListener(nonEditListenerTouch);
                else { // else set touch, long , click listener to button
                    nButton.setOnTouchListener(null);
                    nButton.setOnLongClickListener(this);
                    nButton.setOnClickListener(this);
                }
                layout.addView(nButton);
                if (event.getClipData().getItemAt(0).getText().equals("update")) {
                    dbHelper.updateButton(nButton, "1");
                } else {
                    dbHelper.saveButton(nButton, "1");
                }
            }
            imgButton.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    private int findResourceId(Object tag) {
        if(tag == null) {
            return -1;
        }
        String path = (String) tag;
        for (Field field : R.mipmap.class.getDeclaredFields()) {
            if(path.endsWith(field.getName()+".png")) {
                try {
                    return field.getInt(null);
                } catch (IllegalAccessException e) {
                    Log.e(TAG,"error in findResourceId: " + path,e);
                }
            }
        }
        return 0;
    }

    @Override
    public boolean onLongClick(View v) {
        if(tabLayout.getSelectedTabPosition() != 0) {
            TabLayout.Tab tabAt = tabLayout.getTabAt(0);
            if(tabAt != null) {
                tabAt.select();
            }
        }
        hideCommandLayout();
        unhideButtons();
        ClipData data = ClipData.newPlainText("mode", "update");
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
        v.startDrag(data, shadow, v, 0);
        layout.removeView(v);
        ImageButton imgButton = layout.findViewById(R.id.imageButton4);
        imgButton.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public void onClick(View v) {
        if(commandLayout != null) {
            if(commandLayout.getVisibility() != View.VISIBLE) {
                commandLayout.setVisibility(View.VISIBLE);
                hideButtons();
                fillCommandLayout(String.valueOf(v.getId()));
            } else {
                commandLayout.setVisibility(View.INVISIBLE);
                unhideButtons();
            }
        }
    }


    private ArrayAdapter<String> buttonCommandAdapter;

    private void fillCommandLayout(final String id) {
        ListView buttonCommandList = commandLayout.findViewById(R.id.button_command_list);
        Button remove = commandLayout.findViewById(R.id.const_bt_rmv);
        Button cmd = commandLayout.findViewById(R.id.const_bt_add_cmd);

        buttonCommandsSet = getCommands(id);
        buttonCommandList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        buttonCommandAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_multiple_choice, new ArrayList<>(buttonCommandsSet));
        buttonCommandList.setAdapter(buttonCommandAdapter);

        cmd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CommandActivity.class);
            Bundle b = new Bundle();
            b.putInt("btnId", Integer.parseInt(id)); //Your id
            intent.putExtras(b); //Put your id to your next Intent
            startActivityForResult(intent, ADDCODES);
        });
        remove.setOnClickListener(v -> {
            SparseBooleanArray checkedItemPositions = buttonCommandList.getCheckedItemPositions();
            ArrayList<String> removeObj = new ArrayList<>();
            for(int i =0;i<checkedItemPositions.size();i++) {
                int key = checkedItemPositions.keyAt(i);
                if(checkedItemPositions.get(key)) {
                    removeObj.add(buttonCommandAdapter.getItem(key));
                    buttonCommandList.setItemChecked(i,false);
                    buttonCommandsSet.remove(buttonCommandAdapter.getItem(key));
                }
            }
            for(String obj : removeObj) {
                buttonCommandAdapter.remove(obj);
            }
            buttonCommandAdapter.notifyDataSetChanged();
            removeCommand(id,removeObj);
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

    private void hideButtons() {
        for(int i =0;i<layout.getChildCount();i++) {
            View vv = layout.getChildAt(i);
            if(vv.getId() != commandLayout.getId() && vv.getId() != R.id.imageButton4) {
                if(vv.getY() + vv.getHeight() >= commandLayout.getY()) {
                    vv.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void unhideButtons() {
        for(int i =0;i<layout.getChildCount();i++) {
            View vv = layout.getChildAt(i);
            if(vv.getId() != commandLayout.getId() && vv.getId() != R.id.imageButton4) {
                if(vv.getY() + vv.getHeight() >= commandLayout.getY())
                    vv.setVisibility(View.VISIBLE);
            }
        }
    }



    private Set<String> getCommands(String id) {
        List<Command> btnCommands =   dbHelper.getBtnCommands(id);
        Set<String> set = new HashSet<>();
        for(Command cmd : btnCommands) {
            set.add(cmd.getCommandName());
        }
        return set;
    }

    private void removeCommand(String id,List<String> commands) {
        for(String cmd : commands) {
            dbHelper.removeCommandBtn(id,cmd);
        }
    }

    private void hideCommandLayout() {
        commandLayout = layout.findViewById(R.id.command_set_layout);
        if(commandLayout != null) {
            commandLayout.findViewById(R.id.cnts_btn_ok).setOnClickListener(this);
            commandLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void mainButtonOnclick( int id) {
        EventBus.getDefault().post( new SendCommandDto(SendCommand.api.EXECUTE.name(), BackgroundUtil.getSelectedId(),new HashSet<>(dbHelper.getBtnCommands(Integer.toString(id)))));
    }





}
