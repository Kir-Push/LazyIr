package com.example.buhalo.lazyir.service;

import android.app.IntentService;
import android.app.Notification;
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
import android.support.v4.app.JobIntentService;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.os.Process;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.battery.BatteryBroadcastReveiver;
import com.example.buhalo.lazyir.modules.clipBoard.ClipBoard;
import com.example.buhalo.lazyir.modules.shareManager.ShareModule;
import com.example.buhalo.lazyir.utils.ExtScheduledThreadPoolExecutor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.os.Build.VERSION_CODES.N;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_UNPAIR;


public class BackgroundService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    /**
     * ThreadPool's for basic tasks and short timer tasks.
     */
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);
    private static ConcurrentLinkedQueue<Runnable> takQueue = new ConcurrentLinkedQueue<>();

    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;

    public static int port = 5667;

    // for android 8 notification
    public static final int NotifId = 454352354;

    private static BatteryBroadcastReveiver mReceiver;
    private boolean batteryRegistered;

    // If a Context object is needed, call getApplicationContext() here.
    private static Lock lock = new ReentrantLock();


    private static Context appContext;

    private static volatile boolean started;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
                onHandleWork((Intent) msg.obj);
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
         //   stopSelf(msg.arg1);
        }
    }



    public BackgroundService() {
        super();
        tcp = TcpConnectionManager.getInstance();
        udp = UdpBroadcastManager.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
          //  startForeground(int, android.app.Notification)
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.power_icon)
                    .setContentTitle("Background LazyIr")
                    .setContentText("Sorry android 8 require it");
            if(nm != null)
            nm.notify(NotifId,builder.build());
        }

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    private void initConfig(){
        if(appContext == null)
        appContext = this.getApplicationContext();
        // initialize and setting executors
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);

        ((ThreadPoolExecutor)executorService).setKeepAliveTime(600,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
    }

    private void startTasks(){
        if(started)
            return;
        startUdpListener(port);
        startSendPeriodicallyUdp(port);
       // registerBatteryReceiver();
        startClipboardListener();
        initConfig();
        started = true;
    }

    private void destroy(){
        stopSendingPeriodicallyUdp();
        closeAllTcpConnections();
      //  unregisterBatteryRecever();
        removeClipBoardListener();
        stopSftpServer();
        executorService.shutdownNow();
        timerService.shutdownNow();
        appContext = null;
        started = false;
    }
    /**
     * When service destroyed, stop all network operation's
     * and all running threads.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
      //  destroy(); // not now
    }

    protected void onHandleWork(@NonNull Intent intent) {
        String action = intent.getAction();
        if(action == null)
            return;
        switch (action){
            case "cmd":
                if (cmdAction(intent)) return;
                break;
            case "task":
                taskAction();
                break;
            case "sendToAll":
                sendToAllAction(intent);
                break;
            case "sendToDevice":
                sendToDeviceAction(intent);
                break;
            default:
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    private void sendToDeviceAction(Intent intent){
        String msg = intent.getStringExtra("message");
        String id = intent.getStringExtra("dvId");
        sendToOneDevice(id,msg);
    }


    private void sendToAllAction(Intent intent) {
        String msg = intent.getStringExtra("message");
        sendToAll(msg);
    }

    private void taskAction() {
        Runnable task;
        while ((task = takQueue.poll()) != null)
            executorService.submit(task);
    }

    private boolean cmdAction(Intent intent) {
        String cmd = intent.getStringExtra("cmd");
        int args = intent.getIntExtra("args", -1);
        try {
            Method method = tryToextractMethod(cmd);
            if(method == null)
                return true;
            if(args == -1)
                method.invoke(this);
            else
                method.invoke(this,args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.e("BackgroundService","OnHandleIntentError",e);
        }
        return false;
    }

    private Method tryToextractMethod(String methodName){
        Method method = null;
        try {
            method = this.getClass().getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            try {
                method = this.getClass().getDeclaredMethod(methodName,Integer.class);
            } catch (NoSuchMethodException e1) {
                Log.e("BackgroundServce","Can't find method with such name " + methodName,e);
            }
        }
        if(method != null)
            method.setAccessible(true);
        return method;
    }


    private void startUdpListener(final int port) {
        executorService.submit(()->udp.startUdpListener(appContext,port));
    }

    private void stopUdpListener() {
        executorService.submit(()->udp.stopUdpListener());
    }



    private void startSendPeriodicallyUdp(int port) {
        executorService.submit(()->udp.startSendingTask(appContext,port));
    }

    private void stopSendingPeriodicallyUdp() {
        executorService.submit(()->udp.stopSending());
    }

    private void closeAllTcpConnections(){
        for(Device dv : Device.connectedDevices.values())
            tcp.stopListening(dv);
    }

    private void stopSftpServer(){
        ShareModule.stopSftpServer(); // maybe you need do this something different
    }


    private void removeClipBoardListener() {
        ClipBoard.removeListener(appContext);
    }

    private void startClipboardListener() {
        ClipBoard.setListener(appContext);
    }

    private void unregisterBatteryRecever() {
        unregisterReceiver(mReceiver);
        mReceiver = null;
        batteryRegistered = false;
    }

    private void registerBatteryReceiver() {
        if(mReceiver == null)
            mReceiver = new BatteryBroadcastReveiver();
        if(!batteryRegistered)
        registerReceiver(mReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryRegistered = true;
    }

    private void onZeroConnections(){
        if(Device.getConnectedDevices().size() == 0) { //return to normal frequency
            udp.onZeroConnections();
            MainActivity.setSelected_id("");
            stopSftpServer();
//            unregisterBatteryRecever();
            removeClipBoardListener();
        } else {
            MainActivity.setSelected_id(Device.getConnectedDevices().values().iterator().next().getId());
        }
    }

    private void sendToAll(String message){
        executorService.submit(()->{
        if(Device.getConnectedDevices().size() == 0) {
            return;
        }
        for (Device device : Device.getConnectedDevices().values()) {
            if(device != null && device.isConnected())
            device.sendMessage(message);
        }});
    }


    // todo think need to be in separate thread, or no
    private void sendToOneDevice(String id, String msg) {
        Device.getConnectedDevices().get(id).sendMessage(msg);
    }

    public static void addCommandToQueue(BackgroundServiceCmds cmd){
        Intent intent = new Intent(appContext, BackgroundService.class);
        intent.putExtra("cmd",cmd.name());
        intent.setAction("cmd");
        startServiceOrForeground(intent);
    }

    private static void startServiceOrForeground(Intent intent){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Do something for lollipop and above versions
            appContext.startForegroundService(intent);
        } else{
            appContext.startService(intent);
            // do something for phones running an SDK before lollipop
        }
    }


    public static void sendToAllDevices(String message){
        Intent intent = new Intent(appContext, BackgroundService.class);
        intent.setAction("sendToAll");
        intent.putExtra("message",message);
        startServiceOrForeground(intent);
    }

    public static void sendToDevice(String id,String message){
        Intent intent = new Intent(appContext, BackgroundService.class);
        intent.setAction("sendToDevice");
        intent.putExtra("dvId",id);
        intent.putExtra("message",message);
        startServiceOrForeground(intent);
    }

    public static void submitNewTask(Runnable task){ // todo this for timer
        Intent intent = new Intent(appContext, BackgroundService.class);
        takQueue.add(task);
        intent.setAction("task");
        startServiceOrForeground(intent);
    }


    public static ScheduledThreadPoolExecutor getTimerService() {
        return timerService;
    }

    public static ExecutorService getExecutorService() {return executorService;}

    public static Context getAppContext() {
        return appContext;
    }

    public static void setAppContext(Context appContext) {
        BackgroundService.appContext = appContext;
    }

    public static void unpairDevice(String id) {
        submitNewTask(() -> {
            DBHelper.getInstance(appContext).deletePaired(id);
            NetworkPackage networkPackage = NetworkPackage.Cacher.getOrCreatePackage(TCP_UNPAIR, TCP_UNPAIR);
            BackgroundService.sendToDevice(id,networkPackage.getMessage());
            BackgroundService.unpairDevice(id);
            Device.getConnectedDevices().get(id).setPaired(false);
        });
    }

}
