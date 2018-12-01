package com.example.buhalo.lazyir.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.os.Process;

import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.bus.events.MainActivityCommand;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.device.ModuleSetting;
import com.example.buhalo.lazyir.modules.clipboard.ClipBoard;
import com.example.buhalo.lazyir.modules.notification.notifications.NotificationListenerCmd;
import com.example.buhalo.lazyir.service.receivers.CallReceiver;
import com.example.buhalo.lazyir.service.receivers.NotifActionReceiver;
import com.example.buhalo.lazyir.service.receivers.SmsListener;
import com.example.buhalo.lazyir.service.network.tcp.TcpConnectionManager;
import com.example.buhalo.lazyir.service.network.udp.UdpBroadcastManager;
import com.example.buhalo.lazyir.service.receivers.WifiReceiver;
import com.example.buhalo.lazyir.service.settings.SettingService;
import com.example.buhalo.lazyir.view.UiCmds;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.service.receivers.BatteryBroadcastReceiver;
import com.example.buhalo.lazyir.utils.ExtScheduledThreadPoolExecutor;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import static com.example.buhalo.lazyir.modules.notification.notifications.ShowNotification.api.REMOVE_NOTIFICATION;


public class BackgroundService extends Service {
    public enum api{
        CMD,
        TASK,
        SEND_TO_ALL,
        SEND_TO_DEVICE,
        ARGS,
        MESSAGE,
        CACHE_ID,
        DEVICE_ID
    }

    // for android 8 notification
    public static final int NOTIF_ID = 454352354;
    private static final String NOTIFICATION_CHANNEL_ID_SERVICE = "com.lazyIr.service";

    @Inject @Setter TcpConnectionManager tcp;
    @Inject @Setter UdpBroadcastManager udp;
    @Inject @Setter SettingService settingService;
    @Inject @Setter DBHelper dbHelper;
    @Inject @Setter MessageFactory messageFactory;

    private ServiceHandler mServiceHandler;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    @Getter(value = AccessLevel.PACKAGE)
    private static ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);
    private static ConcurrentLinkedQueue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
    private static ConcurrentHashMap<String,String> messagesCache = new ConcurrentHashMap<>(); // to avoid (android.os.TransactionTooLargeException: data parcel size 3563360 bytes)
    @Getter(value = AccessLevel.PACKAGE)
    private static final ConcurrentHashMap<String, Device> connectedDevices = new ConcurrentHashMap<>();
    @Getter(value = AccessLevel.PACKAGE)
    private static ConcurrentHashMap<String, ModuleSetting> myEnabledModules = new ConcurrentHashMap<>();

    @Getter(value = AccessLevel.PRIVATE) @Setter(value = AccessLevel.PRIVATE)private boolean registered;
    @Getter(value = AccessLevel.PRIVATE) @Setter(value = AccessLevel.PRIVATE) private boolean started;
    @Getter(value = AccessLevel.PRIVATE) @Setter(value = AccessLevel.PRIVATE) private boolean inited;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            initChannel();
            android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID_SERVICE)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle("Background LazyIr")
                    .setContentText("Sorry android 8 require it");
            startForeground(NOTIF_ID,builder.build());
            startSnoozeTask(NOTIF_ID);
        }
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("BackgroundHandlerThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        // Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    private void startSnoozeTask(int notifId) {
        timerService.scheduleAtFixedRate(() ->
                EventBus.getDefault().post(new NotificationListenerCmd(REMOVE_NOTIFICATION, Integer.toString(notifId), null)),
                0,5,TimeUnit.MINUTES);
    }

    /**
     * When service destroyed, stop all network operation's
     * and all running threads.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        setInited(false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if(nm != null) {
                nm.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID_SERVICE, "LazyIr service", NotificationManager.IMPORTANCE_LOW));
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            if(!isInited()) {
                initConfig();
            }
            if(msg != null && msg.obj != null) {
                onHandleWork((Intent) msg.obj);
            }
        }

        private void initConfig(){
            // initialize and setting executors
            timerService.setRemoveOnCancelPolicy(true);
            timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
            timerService.allowCoreThreadTimeOut(true);
            ((ThreadPoolExecutor)executorService).setKeepAliveTime(10,TimeUnit.SECONDS);
            ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
            initEnabedModules();
            setInited(true);
        }

        private void initEnabedModules(){
            List<String>  enabledModules = dbHelper.checkAndSetDefaultIfNoInfo(BackgroundUtil.getMyId());
            Stream.of(enabledModules).forEach(s -> myEnabledModules.put(s,new ModuleSetting(s,true, Collections.emptyList(),true)));
        }
    }



    private void onHandleWork(@NonNull Intent intent) {
        String action = intent.getAction();
        if(action == null) {
            return;
        }
        api command = BackgroundService.api.valueOf(action);
        switch (command){
            case CMD:
                cmdAction(intent);
                break;
            case TASK:
                taskAction();
                break;
            case SEND_TO_ALL:
                sendToAllAction(intent);
                break;
            case SEND_TO_DEVICE:
                sendToDeviceAction(intent);
                break;
            default:
                break;
        }
    }

    private void cmdAction(Intent intent) {
        String cmd = intent.getStringExtra(api.CMD.name());
        BackgroundServiceCmds method = BackgroundServiceCmds.valueOf(cmd);
        switch (method){
            case START_UDP_LISTENER:
                startUdpListener();
                break;
            case STOP_UDP_LISTENER:
                stopUdpListener();
                break;
            case START_SEND_PERIODICALLY_UDP:
                startSendPeriodicallyUdp();
                break;
            case STOP_SENDING_PERIODICALLY_UDP:
                stopSendPeriodicallyUdp();
                break;
            case CLOSE_ALL_TCP_CONNECTIONS:
                closeAllTcpConnections();
                break;
            case DESTROY:
                destroy();
                break;
            case START_TASKS:
                startTasks();
                break;
            case CACHE_CONNECT:
                cacheConnect();
                break;
            case REGISTER_BROADCASTS:
                registerBroadcasts();
                break;
            case ON_DEVICE_DISCONNECTED:
                onDeviceDisconnected();
                break;
            case START_CLIPBOARD_LISTENER:
                startClipboardListener();
                break;
            case REMOVE_CLIP_BOARD_LISTENER:
                removeClipBoardListener();
                break;
            default:
                break;
        }
    }

    private void taskAction() {
        Runnable task;
        while ((task = taskQueue.poll()) != null)
            executorService.submit(task);
    }

    private void sendToAllAction(Intent intent) {
        String msg = intent.getStringExtra(api.MESSAGE.name());
        if(msg == null){
            String cacheId = intent.getStringExtra(api.CACHE_ID.name());
            msg = messagesCache.get(cacheId);
            messagesCache.remove(cacheId);
        }
        sendToAll(msg);
    }

    private void sendToAll(String message){
        executorService.submit(()->{
            for (Device device : connectedDevices.values()) {
                if(device != null && device.isConnected() && device.isPaired())
                    device.sendMessage(message);
            }});
    }

    private void sendToDeviceAction(Intent intent){
        String msg = intent.getStringExtra(api.MESSAGE.name());
        if(msg == null){
            String cacheId = intent.getStringExtra(api.CACHE_ID.name());
            msg = messagesCache.get(cacheId);
            messagesCache.remove(cacheId);
        }
        String id = intent.getStringExtra(api.DEVICE_ID.name());
        sendToOneDevice(id,msg);
    }

    private void sendToOneDevice(String id, String msg) {
        Device device = connectedDevices.get(id);
        if(device != null && device.isConnected()) {
            device.sendMessage(msg);
        }
    }

    private void startTasks(){
        if(isStarted()) {
            return;
        }
        addCommandToQueue(BackgroundServiceCmds.START_UDP_LISTENER);
        addCommandToQueue(BackgroundServiceCmds.START_SEND_PERIODICALLY_UDP);
        setStarted(true);
    }

    private void destroy(){
        addCommandToQueue(BackgroundServiceCmds.CLOSE_ALL_TCP_CONNECTIONS);
        addCommandToQueue(BackgroundServiceCmds.STOP_SENDING_PERIODICALLY_UDP);
        addCommandToQueue(BackgroundServiceCmds.STOP_UDP_LISTENER);
        setStarted(false);
    }

    void addCommandToQueue(BackgroundServiceCmds cmd){
        addCommandToQueue(cmd,getApplicationContext());
    }

    static void addCommandToQueue(BackgroundServiceCmds cmd,Context context){
        Intent intent = new Intent(context, BackgroundService.class);
        String command = api.CMD.name();
        intent.putExtra(command,cmd.name());
        intent.setAction(command);
        startServiceOrForeground(intent,context);
    }

    static void submitNewTask(Runnable task,Context context){
        Intent intent = new Intent(context, BackgroundService.class);
        taskQueue.add(task);
        intent.setAction(api.TASK.name());
        startServiceOrForeground(intent,context);
    }

    static void sendToAllDevices(String message,Context context){
        Intent intent = new Intent(context, BackgroundService.class);
        intent.setAction(api.SEND_TO_ALL.name());
        if(message.length() >= 1200){
            UUID uuid = UUID.randomUUID();
            String s = uuid.toString();
            messagesCache.put(s,message);
            intent.putExtra(api.CACHE_ID.name(),s);
        }
        else {
            intent.putExtra(api.MESSAGE.name(), message);
        }
        startServiceOrForeground(intent,context);
    }


    static void sendToDevice(String id,String message,Context context){
        Intent intent = new Intent(context, BackgroundService.class);
        intent.setAction(api.SEND_TO_DEVICE.name());
        intent.putExtra(api.DEVICE_ID.name(),id);
        if(message.length() >= 1200){
            UUID uuid = UUID.randomUUID();
            String s = uuid.toString();
            messagesCache.put(s,message);
            intent.putExtra(api.CACHE_ID.name(),s);
        }
        else {
            intent.putExtra(api.MESSAGE.name(), message);
        }
        startServiceOrForeground(intent,context);
    }

    private static void startServiceOrForeground(Intent intent,Context context){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Do something for oreo and above versions
            context.startForegroundService(intent);
        } else{
            context.startService(intent);
            // do something for phones running an SDK before oreo
        }
    }

    private int getPort() {
        return Integer.parseInt(settingService.getValue("TCP-port"));
    }


    private void startUdpListener() {
        executorService.submit(()->udp.startUdpListener(getApplicationContext(),getPort()));
    }
    private void stopUdpListener() {
        executorService.submit(()->udp.stopUdpListener());
    }

    private void startSendPeriodicallyUdp() {
        executorService.submit(()->udp.startSendingTask(getApplicationContext(),getPort()));
    }
    private void stopSendPeriodicallyUdp(){
        executorService.submit(()->udp.stopUdpListener());
    }
    private void closeAllTcpConnections(){
        Stream.of(connectedDevices.values()).forEach(tcp::stopListening);
    }

    private void onDeviceDisconnected(){
        Collection<Device> values = getConnectedDevices().values();
        Device next = null;
        if(!values.isEmpty()) {
            next = values.iterator().next();
        }
        BackgroundUtil.setSelectedId(next == null ? "" : next.getId());
        EventBus.getDefault().post(new MainActivityCommand(UiCmds.UPDATE_ACTIVITY,null));

    }


    private void removeClipBoardListener() {
        ClipBoard.removeListener(getApplicationContext());
    }

    private void startClipboardListener() {
        ClipBoard.setListener(getApplicationContext(),messageFactory);
    }

    private void cacheConnect() {
        executorService.submit(()-> udp.cacheConnection());
    }

    private void registerBroadcasts(){
        if(isRegistered()) {
            return;
        }
        setRegistered(true);
        getApplicationContext().registerReceiver(new WifiReceiver(),new IntentFilter("android.net.wifi.supplicant.STATE_CHANGE"));
        getApplicationContext().registerReceiver(new BatteryBroadcastReceiver(),new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        getApplicationContext().registerReceiver(new SmsListener(),new IntentFilter( "android.provider.Telephony.SMS_RECEIVED"));
        getApplicationContext().registerReceiver(new CallReceiver(),new IntentFilter("android.intent.action.PHONE_STATE"));
        getApplicationContext().registerReceiver(new NotifActionReceiver(),new IntentFilter("lazyIr-cmd"));
    }


}
