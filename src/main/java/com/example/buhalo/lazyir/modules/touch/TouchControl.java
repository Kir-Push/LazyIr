package com.example.buhalo.lazyir.modules.touch;


import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

/**
 * Created by buhalo on 21.08.17.
 */
public class TouchControl extends Module {

    private static final String MOVE = "move";
    private static final String CLICK = "click";
    private static final String DCLICK = "dclick";
    private static final String RCLICK = "rclick";
    private static final String MOUSEUP = "mup";
    private static final String MOUSEDOWN = "mdown";
    private static final String MOUSECLICK = "mclick";
    private static final String LONGCLICK = "lclick";
    private static final String LONGRELEASE = "lrelease";


    public TouchControl() throws Exception {

    }


    public void execute(NetworkPackage np) {

    }



    @Override
    public void endWork() {

    }
}
