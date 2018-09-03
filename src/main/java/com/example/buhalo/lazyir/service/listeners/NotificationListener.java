package com.example.buhalo.lazyir.service.listeners;

import android.app.NotificationManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.SparseLongArray;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.notification.NotificationTypes;
import com.example.buhalo.lazyir.modules.notification.messengers.MessengersDto;
import com.example.buhalo.lazyir.modules.notification.notifications.Notification;
import com.example.buhalo.lazyir.modules.notification.NotificationUtils;
import com.example.buhalo.lazyir.modules.notification.notifications.NotificationListenerCmd;
import com.example.buhalo.lazyir.modules.notification.notifications.ShowNotification;
import com.example.buhalo.lazyir.modules.notification.notifications.ShowNotificationCmd;
import com.example.buhalo.lazyir.modules.notification.notifications.ShowNotificationDto;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.BackgroundServiceCmds;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.Getter;
import lombok.Setter;

import static com.example.buhalo.lazyir.service.BackgroundUtil.checkWifiOnAndConnected;


public class NotificationListener extends NotificationListenerService {
    @Inject @Getter @Setter
    NotificationUtils utils;

    public static final String SMS_TYPE = "com.android.mms";
    public static final String SMS_TYPE_2 = "com.android.messaging";

    private SparseLongArray notifsToFrequent = new SparseLongArray();
    private Pattern chargeRegex;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

   @Subscribe(threadMode = ThreadMode.BACKGROUND)
   public void receiveCommand(NotificationListenerCmd cmd){
       ShowNotification.api command = cmd.getCommand();
       if(command.equals(ShowNotification.api.ALL_NOTIFS)){
           sendAllNotifications(cmd.getSenderId());
       }else if(command.equals(ShowNotification.api.REMOVE_NOTIFICATION)){
           createRemoveNotification(cmd.getNotificationId());
       }
   }

    private void createRemoveNotification(String notificationId) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(),"someLittleHack")
                            .setSmallIcon(R.mipmap.up_icon)
                            .setContentTitle(notificationId)
                            .setContentText("I should disappear fast, if no something went wrong");
            NotificationManager mNotifyMgr = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            if(mNotifyMgr != null) {
                mNotifyMgr.notify(999888777, mBuilder.build());
            }
    }

    private boolean checkForRemoveNotification(StatusBarNotification sbn) {
        if(sbn.getId() == 999888777) {
            removeNotification(sbn.getKey());
            StatusBarNotification[] activeNotifications = getActiveNotifications();
            if(activeNotifications != null) {
                for (StatusBarNotification statusBarNotification : activeNotifications) {
                    if (statusBarNotification.getId() == Integer.parseInt(utils.extractTitle(sbn))) {
                        removeNotification(statusBarNotification.getKey());
                    }
                }
            }
            return true;
        }
        return false;
    }


    private void removeNotification(String id){
        cancelNotification(id);
    }

    private void sendAllNotifications(String senderId) {
        StatusBarNotification[] activeNotifications = getActiveNotifications();
        if(activeNotifications != null) {
            List<Notification> notificationList = new ArrayList<>();
            for (StatusBarNotification notification : activeNotifications) {
                notificationList.add(utils.castToMyNotification(notification));
            }
            ShowNotificationDto dto = new ShowNotificationDto(ShowNotification.api.ALL_NOTIFS.name(), notificationList);
            EventBus.getDefault().post(new ShowNotificationCmd(ShowNotification.api.ALL_NOTIFS.name(), senderId, dto));
        }
    }

    private boolean snoozePermamentNotification(StatusBarNotification sbn) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }
        if(sbn.getId() == BackgroundService.NOTIF_ID){
            snoozeNotification(sbn.getKey(), 6000000); //haha
            return true;
        }
        StatusBarNotification[] activeNotifications = getActiveNotifications();
        if (activeNotifications != null) {
            for (StatusBarNotification statusBarNotification : activeNotifications) {
                if (statusBarNotification.getId() == BackgroundService.NOTIF_ID) {
                    snoozeNotification(statusBarNotification.getKey(), 6000000);
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        if (snoozePermamentNotification(sbn) || checkForRemoveNotification(sbn) || !checkWifiOnAndConnected(getApplicationContext())) {
            return;
        }
        Notification notification = utils.castToMyNotification(sbn);
        if (notification == null || utils.isSms(notification)) { // we ignore sms notification's, we have separate sms broadcast receiver
            return;
        }

        if (!BackgroundUtil.hasActualConnection()) {
            BackgroundUtil.addCommand(BackgroundServiceCmds.CACHE_CONNECT, getApplicationContext());
            BackgroundUtil.getTimerExecutor().schedule(() -> processIncomingNotification(notification), 700, TimeUnit.MILLISECONDS);
        } else {
            processIncomingNotification(notification);
        }
    }

    private void processIncomingNotification(Notification notification) {
        String type = notification.getType();
        if(type.equals(NotificationTypes.MESSENGER.name())){
            EventBus.getDefault().post(new MessengersDto(notification));
        }else if(!checkNotificationForDuplicates(notification) && !checkChargingNotification(notification)){
            String receiveCmd = ShowNotification.api.RECEIVE_NOTIFICATION.name();
            EventBus.getDefault().post(new ShowNotificationCmd(receiveCmd,new ShowNotificationDto(receiveCmd,notification)));
        }
    }

    //check whether duplicate notifications was posted around 60 second ago
    private boolean checkNotificationForDuplicates(Notification notification) {
        long currTime = System.currentTimeMillis();
        int hash = notification.hashCode();
        long aLong = notifsToFrequent.get(hash);
        if (notifsToFrequent.size() >= 20) {
            notifsToFrequent.clear();  // don't store too many object's
        }
        notifsToFrequent.put(hash, currTime);
        if (aLong == 0) { // if zero - no data, that means after clearing this notification first - return false
            return false;
        } else {
            long difference = currTime - aLong;
            return difference < 60000;
        }
    }

    // charging notification show each time when percent update, we don't want to see it
    // so filter it
    private boolean checkChargingNotification(Notification notification) {
        if(notification.getPack().equals("com.android.systemui")){ // charging notifs have this packageName
            NotificationManager notificationManager = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
            if(notificationManager == null) {
                return false;
            }
            notificationManager.cancel(Integer.parseInt(notification.getId()));
            if(chargeRegex == null) {
                String regex = "^.+\\d{1,3}%.+$"; // on lg q6 title of notification - Charging (10%)
                chargeRegex = Pattern.compile(regex);
            }
            Matcher matcher = chargeRegex.matcher(notification.getTitle());
            return matcher.matches();
        }
        return false;
    }



    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (!checkWifiOnAndConnected(getApplicationContext())) {
            return;
        }
        Notification notification = utils.castToMyNotification(sbn);
        if (notification != null) {
            if (!BackgroundUtil.hasActualConnection()) {
                BackgroundUtil.addCommand(BackgroundServiceCmds.CACHE_CONNECT, getApplicationContext());
                BackgroundUtil.getTimerExecutor().schedule(() -> processRemoveNotification(notification), 700, TimeUnit.MILLISECONDS);
            } else {
                processRemoveNotification(notification);
            }
            utils.getPendingNotifs().remove(notification.getPack() + ":" + notification.getTitle());
        }
    }

    private void processRemoveNotification(Notification notification) {
        String command = ShowNotification.api.REMOVE_NOTIFICATION.name();
        EventBus.getDefault().post(new ShowNotificationCmd(command,new ShowNotificationDto(command,notification)));
    }

}
