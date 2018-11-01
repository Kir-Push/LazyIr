package com.example.buhalo.lazyir.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.buhalo.lazyir.modules.touch.TouchControl;
import com.example.buhalo.lazyir.modules.touch.TouchControlDto;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.R;

import org.greenrobot.eventbus.EventBus;

public class TouchActivity extends AppCompatActivity {

    private Button leftbtn;
    private Button rightbtn;
    private View touchView;
    private int startx;
    private int starty;
    private double sensitibly = 1;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        touchView = findViewById(R.id.touchView);
        leftbtn = findViewById(R.id.leftBtn);
        rightbtn = findViewById(R.id.rightBtn);
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
    }

    private void move(int x, int y, double sensitibly) {
       int accumX =  (int) (x * sensitibly);
        int accumY = (int) (y * sensitibly);
        if(accumX != 0 && accumY != 0) {
            EventBus.getDefault().post(new TouchControlDto(TouchControl.api.MOVE.name(),id,accumY,accumX));
        }
    }
}
