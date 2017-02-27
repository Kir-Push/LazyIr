package com.example.buhalo.lazyir.UI;

import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by buhalo on 26.02.17.
 */

public class ButtonListener implements View.OnTouchListener {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action)
        {
            case DragEvent.ACTION_DROP:

        }


        return true;
    }
}
