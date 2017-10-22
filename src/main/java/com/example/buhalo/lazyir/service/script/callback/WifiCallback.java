package com.example.buhalo.lazyir.service.script.callback;

import android.content.Context;
import android.content.Intent;

import com.example.buhalo.lazyir.service.script.actions.BaseAction;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by buhalo on 22.10.17.
 */
// callback which using when WIFI statement in script found
public class WifiCallback implements callback {

    // queque of actions, FIFO
    private LinkedBlockingQueue<BaseAction> actionsQueque = new LinkedBlockingQueue<>();

    public void addAction(BaseAction action)
    {
        actionsQueque.add(action);
    }

    // wifi callback will use in BroaadcastListener and maybe in other listeners, therefore in call will passed Receiver or ListenerObject
    // check if object correspond, pop action's and call them.
    @Override
    public boolean call(Object object) {
        BaseAction currAction;
        // poll action while queque become empty
        while ((currAction = actionsQueque.poll()) != null)
        {
            currAction.call(object);
        }
        // for future puproses and tests
        return true;
    }

    @Override
    public boolean call(Context context, Intent intent) {
        return false;
    }
}
