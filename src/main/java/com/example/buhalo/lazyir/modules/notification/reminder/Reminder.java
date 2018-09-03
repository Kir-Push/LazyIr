package com.example.buhalo.lazyir.modules.notification.reminder;

import android.app.NotificationManager;
import android.content.Context;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.notification.notifications.Notification;
import com.example.buhalo.lazyir.modules.notification.NotificationUtils;
import com.example.buhalo.lazyir.modules.notification.sms.Sms;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.service.settings.SettingService;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import lombok.Synchronized;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Reminder extends Module {
    public enum api{
        MISSED_CALLS,
        UNREAD_MESSAGES,
        DISSMIS_ALL_CALLS,
        DISSMIS_ALL_MESSAGES
    }

    private static final ConcurrentHashMap<String,Boolean> ignored = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,Boolean> ignoredCalls = new ConcurrentHashMap<>();
    private static boolean callTask = false;
    private static boolean smsTask = false;
    private static  ScheduledFuture<?> callTaskFuture;
    private static  ScheduledFuture<?> smsTaskFuture;
    private NotificationUtils utls;

    @Inject
    public Reminder(MessageFactory messageFactory, Context context, NotificationUtils utls, SettingService settingService, DBHelper dbHelper) {
        super(messageFactory, context);
        this.utls = utls;
        startReminderTasks(dbHelper,messageFactory,utls,settingService,context);
    }

    @Override
    public void execute(NetworkPackage np) {
        ReminderDto dto = (ReminderDto) np.getData();
        api command = api.valueOf(dto.getCommand());
        switch (command){
            case DISSMIS_ALL_CALLS:
                dismissAllCalls(dto);
                break;
            case DISSMIS_ALL_MESSAGES:
                dismissMessage(dto);
                break;
            default:
                break;

        }
    }

    private void dismissAllCalls(ReminderDto dto) {
        String missecCallsStr = dto.getMissedCallStr();
        if(missecCallsStr != null){
            String[] split = missecCallsStr.split(":::");
            for (String s : split) {
                setCallIgnored(s);
            }
        }
    }

    private void setCallIgnored(String i) {
        ignoredCalls.put(i,false);
    }
    // get MessagesPack from networkPackage, depending of cointained object's - if sms remove from unread log,
    // if messenger message remove notification
    private void dismissMessage(ReminderDto dto) {
        List<Notification> missedNotifications = dto.getNotifications();
        List<Sms> missedSms = dto.getSmsList();
        if(missedSms != null){
            Stream.of(missedSms).forEach(this::setMessageIgnored);
        }
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if(missedNotifications != null && mNotifyMgr != null){
            Stream.of(missedNotifications).forEach(notification -> setMessageRead(notification,mNotifyMgr));
        }
    }

    private void setMessageRead(Notification notification, NotificationManager notificationManager){
        utls.getPendingNotifs().remove(notification.getPack() + ":" + notification.getTitle());
        notificationManager.cancel(Integer.parseInt(notification.getId())); // todo test, and if didn't work, do though post notification (send notificaiton with id, and in notif listener remove posted notif and notif by contained id)
    }

    private void setMessageIgnored(Sms sms) {
        ignored.put(sms.getId(),false);
    }

    @Synchronized
    private static void setCallReminerTask(int callFrequency,DBHelper dbHelper,MessageFactory messageFactory,Context context){
        if (callTask || callTaskFuture != null) {
            return;
        }
        callTask = true;
        Runnable task = () -> {
            List<MissedCall> missedCalls = dbHelper.getMissedCalls(); // fetch missed call's
            Iterator<MissedCall> iterator = missedCalls.iterator();
            while (iterator.hasNext()) {
                String id = iterator.next().getId();
                if (ignoredCalls.containsKey(id)) {
                    iterator.remove();
                    ignoredCalls.put(id, true);
                }
            }
            if (!missedCalls.isEmpty()) {
                String message = messageFactory.createMessage(Reminder.class.getSimpleName(), true, new ReminderDto(api.MISSED_CALLS.name(), missedCalls));
                BackgroundUtil.sendToAll(message, context);
            }
            clearIgnoreList(ignoredCalls);
        };
        callTaskFuture = BackgroundUtil.getTimerExecutor().scheduleWithFixedDelay(task, 0, callFrequency, TimeUnit.SECONDS);
    }
     /*
     clear ignored list from messages which are not actuall now
    (if you returned not readed messages, and some message in ignore map didn't contain in this list, so this message already readed and you remove it from ignore list
      */
    private static void clearIgnoreList(ConcurrentHashMap<String,Boolean> ignored) {
        Iterator<Map.Entry<String, Boolean>> iterator = ignored.entrySet().iterator();
        while(iterator.hasNext()){
            if(!iterator.next().getValue()){
                iterator.remove();
            }
        }
    }

    @Synchronized
    private static void setSmsReminderTask(int smsFrequency,DBHelper dbHelper,MessageFactory messageFactory,NotificationUtils utls,Context context){
        if (smsTask || smsTaskFuture != null) {
            return;
        }
        smsTask = true;
        Runnable task = () -> {
            List<Sms> unreadMessages = dbHelper.getUnreadMessages(); // fetch unread Messages
            setIgnored(unreadMessages.iterator(), ignored);
            unreadMessages.addAll(dbHelper.getUnreadMMs());
            List<Notification> pendNotif = Stream.of(utls.getPendingNotifs().values()).map(utls::castToMyNotification).collect(Collectors.toList());
            if (!unreadMessages.isEmpty() || !pendNotif.isEmpty()) {
                BackgroundUtil.sendToAll(messageFactory.createMessage(Reminder.class.getSimpleName(), true, new ReminderDto(api.UNREAD_MESSAGES.name(), pendNotif.isEmpty() ? null : pendNotif, unreadMessages.isEmpty() ? null : unreadMessages)), context);
            }
            clearIgnoreList(ignored);
        };
        smsTaskFuture = BackgroundUtil.getTimerExecutor().scheduleWithFixedDelay(task, 0, smsFrequency, TimeUnit.SECONDS);
    }

    private static void setIgnored(Iterator<Sms> iterator, ConcurrentHashMap<String, Boolean> ignored) {
        while (iterator.hasNext()) {
            String id = iterator.next().getId();
            if (ignored.containsKey(id)) {
                iterator.remove();
                ignored.put(id, true);
            }
        }
    }

    @Synchronized
    private static void stopCallReminderTask(){
        if (callTaskFuture != null) {
            callTaskFuture.cancel(true);
        }
        callTaskFuture = null;
        callTask = false;
    }

    @Synchronized
    private static void stopSmsReminderTask() {
        if (smsTaskFuture != null) {
            smsTaskFuture.cancel(true);
        }
        smsTaskFuture = null;
        smsTask = false;
    }


    @Synchronized
    private static void startReminderTasks(DBHelper dbHelper,MessageFactory messageFactory,NotificationUtils utls,SettingService settingService,Context context){
        setCallReminerTask(Integer.parseInt(settingService.getValue("callFrequency")),dbHelper,messageFactory,context);
        setSmsReminderTask(Integer.parseInt(settingService.getValue("smsFrequency")),dbHelper,messageFactory,utls,context);
    }

    @Override
    public void endWork() {
        EventBus.getDefault().unregister(this);
        if(BackgroundUtil.ifLastConnectedDeviceAreYou(device.getId())){
            stopCallReminderTask();
            stopSmsReminderTask();
        }
    }
}
