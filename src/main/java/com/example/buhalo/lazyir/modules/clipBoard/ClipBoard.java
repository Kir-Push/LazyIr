package com.example.buhalo.lazyir.modules.clipBoard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

/**
 * Created by buhalo on 18.04.17.
 */

public class ClipBoard extends Module {
    private static String RECEIVE = "receive";
    private static volatile boolean inserted = false;

    private static ClipListener clipListener;

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(RECEIVE))
        {
            onReceive(np);
        }

    }

    private void onReceive(NetworkPackage np) {
        inserted = true;
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", np.getValue("text"));
        clipboard.setPrimaryClip(clip);
    }

    public static void setListener(Context context)
    {
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipListener == null)
        {
            clipListener = new ClipListener(clipboard);
            clipboard.addPrimaryClipChangedListener(clipListener);
        }
    }

    public static void removeListener(Context context)
    {
        if(clipListener == null)
            return;
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.removePrimaryClipChangedListener(clipListener);
        clipListener = null;
    }

    private static class ClipListener implements ClipboardManager.OnPrimaryClipChangedListener
    {
        final ClipboardManager clipboard;

        public ClipListener(ClipboardManager clipboard)
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
                NetworkPackage np = new NetworkPackage(ClipBoard.class.getSimpleName(), RECEIVE);
                np.setValue("text", text);
                TcpConnectionManager.getInstance().sendCommandToAll(np.getMessage());
            }
        }
    }
}
