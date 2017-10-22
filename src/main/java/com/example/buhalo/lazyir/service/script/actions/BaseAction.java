package com.example.buhalo.lazyir.service.script.actions;

import android.content.Context;
import android.content.Intent;

/**
 * Created by buhalo on 22.10.17.
 */

public interface BaseAction {

    boolean call(Object... args);

    boolean call(Context context, Intent intent);
}
