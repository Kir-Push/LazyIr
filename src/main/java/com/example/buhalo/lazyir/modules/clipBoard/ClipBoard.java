package com.example.buhalo.lazyir.modules.clipBoard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 18.04.17.
 */

public class ClipBoard extends Module {
    private static String RECEIVE = "receive";
    private static volatile boolean inserted = false;

    private static ClipListener clipListener;
    private static Lock staticlock = new ReentrantLock();

    @Override
    public void execute(NetworkPackage np) {
        if(!working)
            return;
        if(np.getData().equals(RECEIVE)) {
            onReceive(np);
        }

    }


    private void onReceive(NetworkPackage np) {
        staticlock.lock();
        try {
            inserted = true;
            ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", np.getValue("text"));
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
        }finally {
            staticlock.unlock();
        }
    }

    public static void setListener(Context context)
    {
        staticlock.lock();
        try{
        final ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard != null) {
            clipListener = new ClipListener(clipboard);
            clipboard.addPrimaryClipChangedListener(clipListener);
        }}finally {
            staticlock.unlock();
        }
    }

    public static void removeListener(Context context)
    {
        staticlock.lock();
        try {
            final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipListener == null || clipboard == null)
                return;
            clipboard.removePrimaryClipChangedListener(clipListener);
            clipListener = null;
        }finally {
            staticlock.unlock();
        }
    }

    private static class ClipListener implements ClipboardManager.OnPrimaryClipChangedListener
    {
        final ClipboardManager clipboard;

        ClipListener(ClipboardManager clipboard)
        {
            this.clipboard = clipboard;
        }

        @Override
        public void onPrimaryClipChanged() {
            if(inserted) {
                inserted = false;
                return;
            }
            ClipData clipData = clipboard.getPrimaryClip();
            ClipData.Item item = clipData.getItemAt(0);
            if(item != null && item.getText() != null) {
                String text = item.getText().toString();
                NetworkPackage np =   NetworkPackage.Cacher.getOrCreatePackage(ClipBoard.class.getSimpleName(), RECEIVE);
                np.setValue("text", text);
                BackgroundService.sendToAllDevices(np.getMessage());
            }
        }
    }
}
