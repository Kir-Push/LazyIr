package com.example.buhalo.lazyir.modules.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Looper;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import org.greenrobot.eventbus.EventBus;


import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

import static com.example.buhalo.lazyir.modules.clipboard.ClipBoard.api.RECEIVE;

public class ClipBoard extends Module {
    public enum api{
        RECEIVE
    }
    @Getter @Setter
    private static ClipListener clipListener;
    @Setter(AccessLevel.PRIVATE) @Getter(AccessLevel.PRIVATE)
    private static boolean inserted;

    @Inject
    public ClipBoard(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
        BackgroundUtil.addCommand(BackgroundServiceCmds.START_CLIPBOARD_LISTENER,context);
    }

    @Override
    public void execute(NetworkPackage np) {
        ClipBoardDto dto = (ClipBoardDto) np.getData();
        if(dto.getCommand().equals(RECEIVE.name())) {
            onReceive(dto);
        }
    }
    @Override
    public void endWork() {
        if(BackgroundUtil.ifLastConnectedDeviceAreYou(device.getId())){
            BackgroundUtil.addCommand(BackgroundServiceCmds.REMOVE_CLIP_BOARD_LISTENER,context);
        }
        device = null;
        context = null;
    }

    private void onReceive(ClipBoardDto dto) {
        setInserted(true);
            ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", dto.getText());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
    }

    @Synchronized
    public static void setListener(Context context,MessageFactory messageFactory) {
        if( getClipListener() == null){
            final ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && getClipListener() == null) {
                    setClipListener(new ClipListener(clipboard, messageFactory, context));
                    clipboard.addPrimaryClipChangedListener(getClipListener());
                }
        }
    }

    @Synchronized
    public static void removeListener(Context context) {
            final ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard != null && clipListener != null) {
            clipboard.removePrimaryClipChangedListener(clipListener);
        }
        clipListener = null;
    }

    private static class ClipListener implements ClipboardManager.OnPrimaryClipChangedListener
    {
        final ClipboardManager clipboard;
        final MessageFactory messageFactory;
        final Context context;

        ClipListener(ClipboardManager clipboard,MessageFactory messageFactory,Context context) {
            this.clipboard = clipboard;
            this.messageFactory = messageFactory;
            this.context = context;
        }

        @Override
        public void onPrimaryClipChanged() {
            if(isInserted()) {
               setInserted(false);
               return;
            }
            ClipData clipData = clipboard.getPrimaryClip();
            ClipData.Item item = clipData.getItemAt(0);
            if(item != null && item.getText() != null) {
                String text = item.getText().toString();
                String message = messageFactory.createMessage(ClipBoard.class.getSimpleName(), true, new ClipBoardDto(RECEIVE.name(), text));
                BackgroundUtil.sendToAll(message,context);
            }
        }
    }
}
