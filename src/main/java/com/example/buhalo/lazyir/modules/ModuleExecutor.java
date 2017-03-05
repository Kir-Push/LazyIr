package com.example.buhalo.lazyir.modules;

import com.example.buhalo.lazyir.Devices.NetworkPackage;

/**
 * Created by buhalo on 05.03.17.
 */

public class ModuleExecutor {


    public static void executePackage(final NetworkPackage np)
    {
        String type = np.getType();
        final Module module = np.getDv().getEnabledModules().get(type);
        if(module == null)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                module.execute(np);
            }
        }).start();
    }

}
