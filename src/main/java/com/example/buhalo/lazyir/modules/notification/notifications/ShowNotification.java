package com.example.buhalo.lazyir.modules.notification.notifications;

import android.content.Context;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


public class ShowNotification extends Module {
    public enum api{
        RECEIVE_NOTIFICATION,
        ALL_NOTIFS,
        NOTIFICATION_ID,
        SHOW_NOTIFICATION,
        REMOVE_NOTIFICATION
    }

    @Inject
    public ShowNotification(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void execute(NetworkPackage np) {
        ShowNotificationDto dto = (ShowNotificationDto) np.getData();
        String command = dto.getCommand();
        if (command.equals(api.ALL_NOTIFS.name())) {
            EventBus.getDefault().post(new NotificationListenerCmd(api.ALL_NOTIFS,device.getId()));
        } else if (command.equals(api.REMOVE_NOTIFICATION.name())) {
            EventBus.getDefault().post(new NotificationListenerCmd(api.REMOVE_NOTIFICATION, dto.getNotification().getId(),device.getId()));
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void receiveCommandFromListener(ShowNotificationCmd command){
        String cmd = command.getCmd();
        if((cmd.equals(api.ALL_NOTIFS.name()) && command.getId().equals(device.getId()))
                || (cmd.equals(api.RECEIVE_NOTIFICATION.name()) || cmd.equals(api.REMOVE_NOTIFICATION.name()))){
            sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,command.getDto()));
        }
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
    }


}
