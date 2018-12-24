package com.example.buhalo.lazyir.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.buhalo.lazyir.modules.touch.KeyboardControl;
import com.example.buhalo.lazyir.modules.touch.KeyboardDto;
import com.example.buhalo.lazyir.modules.touch.TouchControl;
import com.example.buhalo.lazyir.modules.touch.TouchControlDto;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.R;

import org.greenrobot.eventbus.EventBus;

public class TouchActivity extends AppCompatActivity {

    private Button leftbtn;
    private Button rightbtn;
    private Button showKeyboard;
    private EditText editText;
    private View touchView;
    private int startx;
    private int starty;
    private double sensitibly = 1;
    private boolean keyboardShowed = false;
    private boolean keyEventDouble = false;
    private String id = BackgroundUtil.getSelectedId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!BackgroundUtil.hasActualConnection()) {
            Toast.makeText(this,"No connection",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.touch_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        touchView = findViewById(R.id.touchView);
        leftbtn = findViewById(R.id.leftBtn);
        rightbtn = findViewById(R.id.rightBtn);
        showKeyboard = findViewById(R.id.show_keyboard);
        editText = findViewById(R.id.editText);
        setListeners();
    }

    private void setListeners() {
        touchView.setOnTouchListener((v, event) -> {
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startx = x;
                    starty = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    move(x - startx, y - starty, sensitibly);
                    starty = y;
                    startx = x;
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                default:
                    break;
            }
            return true;
        });
        leftbtn.setOnClickListener(v -> EventBus.getDefault().post(new TouchControlDto(TouchControl.api.CLICK.name(),id)));
        rightbtn.setOnClickListener(v -> EventBus.getDefault().post(new TouchControlDto(TouchControl.api.RCLICK.name(),id)));
        showKeyboard.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(!keyboardShowed) {
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
                keyboardShowed = true;
            }else{
               imm.hideSoftInputFromWindow( getCurrentFocus().getWindowToken(),0);
                editText.clearFocus();
                keyboardShowed = false;
            }
        });
        editText.setFocusable(true);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((s.length() == 1 && s.charAt(0) != '\n')) {
                    EventBus.getDefault().post(new KeyboardDto(KeyboardControl.api.PRESS.name(), s.charAt(0), id));
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // don't need it
            }
            @Override
            public void afterTextChanged(Editable s) {
                s.clear();
            }
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            if(!keyEventDouble){
                String character = "";
                if(keyCode == 67){
                    character = "backspace";
                }
                else if(keyCode == 66){
                    character = "enter";
                }
                EventBus.getDefault().post(new KeyboardDto(KeyboardControl.api.SPECIAL_KEYS.name(), character, id));
                keyEventDouble = true;
            }else{
                keyEventDouble = false;
            }
            return false;
        });
    }

    private void move(int x, int y, double sensitibly) {
       int accumX =  (int) (x * sensitibly);
        int accumY = (int) (y * sensitibly);
        if(accumX != 0 && accumY != 0) {
            EventBus.getDefault().post(new TouchControlDto(TouchControl.api.MOVE.name(),id,accumY,accumX));
        }
    }
}
