package com.example.buhalo.lazyir.modules.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private DBHelper dbHelper;
    @Getter @Setter
    private static ClipListener clipListener;
    @Setter(AccessLevel.PRIVATE) @Getter(AccessLevel.PRIVATE)
    private static boolean inserted;

    @Inject
    public ClipBoard(MessageFactory messageFactory, Context context,DBHelper dbHelper) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
        this.dbHelper = dbHelper;
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
        EventBus.getDefault().unregister(this);
        device = null;
        context = null;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void commandFromActivity(ClipBoardDto dto) {
     if(device.getId().equalsIgnoreCase(BackgroundUtil.getSelectedId()) && dto.getCommand().equalsIgnoreCase(RECEIVE.name())){
         onReceive(dto);
     }
    }

        private void onReceive(ClipBoardDto dto) {
        setInserted(true);
            ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", dto.getText());
            dbHelper.saveClipboard(new ClipboardDB(dto.getText(),device.getName(),-1));
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
    }

    @Synchronized
    public static void setListener(Context context,MessageFactory messageFactory,DBHelper dbHelper) {
        if( getClipListener() == null){
            final ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && getClipListener() == null) {
                    setClipListener(new ClipListener(clipboard, messageFactory, context,dbHelper));
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
        final DBHelper dbHelper;
        final Context context;

        ClipListener(ClipboardManager clipboard,MessageFactory messageFactory,Context context,DBHelper dbHelper) {
            this.clipboard = clipboard;
            this.messageFactory = messageFactory;
            this.context = context;
            this.dbHelper = dbHelper;
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
                dbHelper.saveClipboard(new ClipboardDB(text,BackgroundUtil.getMyName(),-1));
                BackgroundUtil.sendToAll(message,context);
            }
        }
    }
}
