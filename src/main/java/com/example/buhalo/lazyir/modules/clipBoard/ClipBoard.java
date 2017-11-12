package com.example.buhalo.lazyir.modules.clipBoard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
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
    private static Lock lock = new ReentrantLock();

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(RECEIVE)) {
            onReceive(np);
        }

    }

    // you actually do not need do something there, because
    // module need be ended only when connected device are 0
    // and this will be done by background onZeroConnections method.
    @Override
    public void endWork() {

    }

    private void onReceive(NetworkPackage np) {
        lock.lock();
        try {
            inserted = true;
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", np.getValue("text"));
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
        }finally {
            lock.unlock();
        }
    }

    public static void setListener(Context context)
    {
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipListener == null && clipboard != null)
        {
            clipListener = new ClipListener(clipboard);
            clipboard.addPrimaryClipChangedListener(clipListener);
        }
    }

    public static void removeListener(Context context)
    {

        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipListener == null || clipboard == null)
            return;
            clipboard.removePrimaryClipChangedListener(clipListener);

        clipListener = null;
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
            if(inserted)
            {
                inserted = false;
                return;
            }
            ClipData clipData = clipboard.getPrimaryClip();
            ClipData.Item item = clipData.getItemAt(0);
            if(item != null && item.getText() != null) {
                String text = item.getText().toString();
                NetworkPackage np =   NetworkPackage.Cacher.getOrCreatePackage(ClipBoard.class.getSimpleName(), RECEIVE);
                np.setValue("text", text);
                TcpConnectionManager.getInstance().sendCommandToAll(np.getMessage());
            }
        }
    }
}
