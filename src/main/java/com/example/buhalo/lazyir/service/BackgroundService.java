package com.example.buhalo.lazyir.service;

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
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.os.Process;
import android.widget.Toast;

import com.example.buhalo.lazyir.DbClasses.DBHelper;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.ModuleSetting;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.modules.battery.BatteryBroadcastReveiver;
import com.example.buhalo.lazyir.modules.clipBoard.ClipBoard;
import com.example.buhalo.lazyir.modules.shareManager.ShareModule;
import com.example.buhalo.lazyir.utils.ExtScheduledThreadPoolExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_PAIR_RESULT;
import static com.example.buhalo.lazyir.service.TcpConnectionManager.TCP_UNPAIR;


public class BackgroundService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    /**
     * ThreadPool's for basic tasks and short timer tasks.
     */
    private static  ExecutorService executorService = Executors.newCachedThreadPool();
    private static  ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);
    private static ConcurrentLinkedQueue<Runnable> takQueue = new ConcurrentLinkedQueue<>();

    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;
    private static SettingService settingService;

    private static int port = 0;

    // for android 8 notification
    public static final int NotifId = 454352354;

    private static BatteryBroadcastReveiver mReceiver;
    private boolean batteryRegistered;

    // If a Context object is needed, call getApplicationContext() here.
    private static Lock lock = new ReentrantLock();


    private static Context appContext = getAppContext();
    private static volatile boolean started;
    private static volatile boolean inited;

    private static ConcurrentHashMap<String,String> messagesCache = new ConcurrentHashMap<>(); // to avoid (android.os.TransactionTooLargeException: data parcel size 3563360 bytes)

    public static SettingService getSettingManager() {
        lock.lock();
        try {
            if (settingService == null)
                settingService = new SettingService();
            return settingService;
        }finally {
            lock.unlock();
        }
    }


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            if(!inited)
                initConfig(getApplicationContext());
            if(msg != null && msg.obj != null)
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

     private void initConfig(Context context){
        if(appContext == null)
        appContext = context.getApplicationContext();
        // initialize and setting executors
         if(timerService == null)
             timerService = new ExtScheduledThreadPoolExecutor(5);
         if(executorService == null)
             executorService = Executors.newCachedThreadPool();
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);

        ((ThreadPoolExecutor)executorService).setKeepAliveTime(600,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
        inited = true;
    }

    private void initConfig(){
         if(timerService == null)
             timerService = new ExtScheduledThreadPoolExecutor(5);
        if(executorService == null)
            executorService = Executors.newCachedThreadPool();
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);

        ((ThreadPoolExecutor)executorService).setKeepAliveTime(600,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
    }

    private void startTasks(){
        if(started)
            return;
        BackgroundService.addCommandToQueue(BackgroundServiceCmds.startUdpListener);
        BackgroundService.addCommandToQueue(BackgroundServiceCmds.startSendPeriodicallyUdp);
        BackgroundService.addCommandToQueue(BackgroundServiceCmds.startClipboardListener);
       // registerBatteryReceiver();
        started = true;
    }

    private void destroy(){
        BackgroundService.addCommandToQueue(BackgroundServiceCmds.closeAllTcpConnections);
        BackgroundService.addCommandToQueue(BackgroundServiceCmds.stopSendingPeriodicallyUdp);
      //  unregisterBatteryRecever();
        BackgroundService.addCommandToQueue(BackgroundServiceCmds.removeClipBoardListener);
        BackgroundService.addCommandToQueue(BackgroundServiceCmds.stopSftpServer);
        BackgroundService.addCommandToQueue(BackgroundServiceCmds.stopUdpListener);
        started = false;
    }
    /**
     * When service destroyed, stop all network operation's
     * and all running threads.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        inited = false;
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
        if(msg == null){
            String cacheId = intent.getStringExtra("cacheId");
            msg = messagesCache.get(cacheId);
            messagesCache.remove(cacheId);
        }
        String id = intent.getStringExtra("dvId");
        sendToOneDevice(id,msg);
    }


    private void sendToAllAction(Intent intent) {
        String msg = intent.getStringExtra("message");
        if(msg == null){
            String cacheId = intent.getStringExtra("cacheId");
            msg = messagesCache.get(cacheId);
            messagesCache.remove(cacheId);
        }
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


    private void startUdpListener() {
        executorService.submit(()->udp.startUdpListener(appContext,getPort()));
    }

    private void stopUdpListener() {
        executorService.submit(()->udp.stopUdpListener());
    }



    private void startSendPeriodicallyUdp() {
        executorService.submit(()->udp.startSendingTask(appContext,getPort()));
    }

    private void stopSendingPeriodicallyUdp() {
        executorService.submit(()->udp.stopSending());
    }

    private void closeAllTcpConnections(){
        try {
            for (Device dv : Device.getConnectedDevices().values())
                tcp.stopListening(dv);
        }catch (Throwable e){
            Log.e("BackgroundService","closeAllTcpConnections ",e);
        }
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
   //         removeClipBoardListener();
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
        Device device = Device.getConnectedDevices().get(id);
        if(device != null && device.isConnected())
        device.sendMessage(msg);
        else
            showNoConnection();
    }

    private void showNoConnection() {
        Toast toast = Toast.makeText(getApplicationContext(), "Sorry no connection",Toast.LENGTH_SHORT );
        toast.show();
    }

    private void cacheConnect() {
        executorService.submit(()->{
            udp.cacheConnection();
        });
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
        if(message.length() >= 512000){
            UUID uuid = UUID.randomUUID();
            String s = uuid.toString();
            messagesCache.put(s,message);
            intent.putExtra("cacheId",s);
        }
        else
        intent.putExtra("message",message);
        startServiceOrForeground(intent);
    }

    public static void sendToDevice(String id,String message){
        Intent intent = new Intent(appContext, BackgroundService.class);
        intent.setAction("sendToDevice");
        intent.putExtra("dvId",id);
        if(message.length() >= 512000){
            UUID uuid = UUID.randomUUID();
            String s = uuid.toString();
            messagesCache.put(s,message);
            intent.putExtra("cacheId",s);
        }
        else
        intent.putExtra("message",message);
        startServiceOrForeground(intent);
    }

    public static void submitNewTask(Runnable task){ // todo this for timer
        Intent intent = new Intent(appContext, BackgroundService.class);
        takQueue.add(task);
        intent.setAction("task");
        startServiceOrForeground(intent);
    }

    public static HashSet<String> getMyEnabledModules(){
        return ModuleFactory.getMyEnabledModules(getAppContext());
    }

    public static List<ModuleSetting> getMyEnabledModulesToModuleSetting(){
        HashSet<String> myEnabledModules = ModuleFactory.getMyEnabledModules(getAppContext());
        List<ModuleSetting> list = new ArrayList<>();
        for (String myEnabledModule : myEnabledModules) {
            list.add(new ModuleSetting(myEnabledModule,true,"*",true));
        }
        return list;
    }


    public static ScheduledThreadPoolExecutor getTimerService() {
        return timerService;
    }

    public static ExecutorService getExecutorService() {return executorService;}

    public static Context getAppContext() {
        return appContext;
    }

    public static void setAppContext(Context cntx) {appContext = cntx;
    }

    public static void unpairDevice(String id) {
        submitNewTask(() -> {
            DBHelper.getInstance(appContext).deletePaired(id);
            NetworkPackage networkPackage = NetworkPackage.Cacher.getOrCreatePackage(TCP_UNPAIR, TCP_UNPAIR);
            BackgroundService.sendToDevice(id,networkPackage.getMessage());
            Device.getConnectedDevices().get(id).setPaired(false);
            MainActivity.updateActivity(); // at this moment, server not respond to unpair, so update here
        });
    }

    public static void pairDevice(String id, String value){
        submitNewTask(() -> {
            Device device = Device.getConnectedDevices().get(id);
            DBHelper.getInstance(getAppContext()).savePairedDevice(id, value);
                    if (device != null)
                        device.setPaired(true);
            NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(TCP_PAIR_RESULT, String.valueOf(NetworkPackage.getMyId().hashCode()));
            orCreatePackage.setValue("answer","paired");
            sendToDevice(id,orCreatePackage.getMessage());
            MainActivity.updateActivity();
        });
    }


    public static int getPort() {
        lock.lock();
        try{
            if(port == 0)
                port = Integer.parseInt(getSettingManager().getValue("TCP-port"));
        }finally {
            lock.unlock();
        }
        return port;
    }

    public static boolean  hasActualConnection(){
        boolean hasConnectedDevice = false;
        if(Device.getConnectedDevices().size() != 0){
            for (Device device : Device.getConnectedDevices().values()) {
                if(device.isConnected()){
                    hasConnectedDevice = true;
                    break;
                }
            }
        }
        return hasConnectedDevice;
    }

}
