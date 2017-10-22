package com.example.buhalo.lazyir.service.script.callback;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.example.buhalo.lazyir.service.script.actions.BaseAction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by buhalo on 22.10.17.
 */

public class BroadcastCallback implements callback {
    private ConcurrentHashMap<String,LinkedBlockingQueue<BaseAction>> actionsMap = new ConcurrentHashMap<>();

    // these twoo action may be the same, check two always
    private static final String WIFI_STATE = WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
    private static final String WIFI_STATE_2 = WifiManager.WIFI_STATE_CHANGED_ACTION;

    public void addAction(BaseAction action)
    {

    }

    public void addAction(String actionType,BaseAction action)
    {
        // always use only first state, if second, set to first
        // you don't know what state will be, but you handle them equaly
        if(actionType.equals(WIFI_STATE) || actionType.equals(WIFI_STATE_2))
            actionType = WIFI_STATE;

        LinkedBlockingQueue<BaseAction> baseActions = actionsMap.get(actionType);

        if(baseActions == null)
            baseActions = new LinkedBlockingQueue<>();

        baseActions.add(action);
        actionsMap.put(actionType,baseActions);
    }

    @Override
    public boolean call(Object object) {
        return true;
    }

    @Override
    public boolean call(Context context, Intent intent) {

        // always use only first state, if second, set to first
        // you don't know what state will be, but you handle them equaly
        String action = intent.getAction();
        System.out.println("CALLING CALLBACK!!!!   " + action);
        if(action.equals(WIFI_STATE_2))
            action = WIFI_STATE;
        //get baseActions from Map, if null return - not actions for this type broadcast
        // else execute returned actions
        LinkedBlockingQueue<BaseAction> baseActions = actionsMap.get(action);
        if(baseActions == null)
            return false;

        System.out.println("CALLING CALLBACK AFTER CHECK!!!!");
        // poll action while queque become empty
        for (BaseAction currAction : baseActions) {
            currAction.call(context,intent);
        }

        // for future puproses and tests
        return true;
    }
}
