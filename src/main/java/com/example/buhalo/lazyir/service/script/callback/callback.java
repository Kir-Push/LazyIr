package com.example.buhalo.lazyir.service.script.callback;

import android.content.Context;
import android.content.Intent;

import com.example.buhalo.lazyir.service.script.actions.BaseAction;

/**
 * Created by buhalo on 22.10.17.
 */

public interface callback {

    void addAction(BaseAction action);
    // in implementation will using actual object
    boolean call(Object object);

    boolean call(Context context, Intent intent);
}
