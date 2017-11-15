package com.example.buhalo.lazyir.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.MainActivity;
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

/**
 * Created by buhalo on 14.11.17.
 */

public class BackgroundService extends IntentService {

    /**
     * ThreadPool's for basic tasks and short timer tasks.
     */
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);

    private static ConcurrentLinkedQueue<Runnable> takQueue = new ConcurrentLinkedQueue<>();

    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;

    private BatteryBroadcastReveiver mReceiver;
    private boolean batteryRegistered;

    // If a Context object is needed, call getApplicationContext() here.
    private static Lock lock = new ReentrantLock();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BackgroundService(String name) {
        super(name);
        tcp = TcpConnectionManager.getInstance();
        udp = UdpBroadcastManager.getInstance();
    }

    /**
     * Default constructor, need for autostart
     *
     * Uses default name for worker thread
     */
    public BackgroundService() {
        super("backgroundWorker");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize and setting executors
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);

        ((ThreadPoolExecutor)executorService).setKeepAliveTime(10,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
    }

    /**
     * When service destroyed, stop all network operation's
     * and all running threads.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTcpListener();
        stopSendingPeriodicallyUdp();
        closeAllTcpConnections();
        unregisterBatteryRecever();
        removeClipBoardListener();
        stopSftpServer();
        executorService.shutdownNow();
        timerService.shutdownNow();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null)
            return;
        String action = intent.getAction();
        if(action != null && action.equals("cmd")){
            String cmd = intent.getStringExtra("cmd");
            int args = intent.getIntExtra("args", -1);

                try {
                    Method method = tryToextractMethod(cmd);
                    if(args == -1)
                         method.invoke(this);
                    else
                        method.invoke(this,args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Log.e("BackgroundService","OnHandleIntentError",e);
                }

        } else if(action != null && action.equals("task")) {

            Runnable task;
            while ((task = takQueue.poll()) != null)
                executorService.submit(task);
        }
    }

    private Method tryToextractMethod(String methodName){
        Method method = null;
        try {
            method = this.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            try {
                method = this.getClass().getMethod(methodName,Integer.class);
            } catch (NoSuchMethodException e1) {
                Log.e("BackgroundServce","Can't find method with such name " + methodName,e);
            }
        }
        return method;
    }


    private void startUdpListener(final int port) {
        executorService.submit(()->udp.startUdpListener(getApplicationContext(),port));
    }

    private void stopUdpListener() {
        udp.stopUdpListener();
    }

    private void stopTcpListener() {
        tcp.stopListening();
    }

    private void startListeningTcp(int port) {
        executorService.submit(()->tcp.startListening(port,getApplicationContext()));
    }


    private void startSendPeriodicallyUdp(int port) {
        executorService.submit(()->udp.startSendingTask(getApplicationContext(),port));
    }

    private void stopSendingPeriodicallyUdp() {
        udp.stopSending();
    }

    private void closeAllTcpConnections(){
        for(Device dv : Device.connectedDevices.values())
            tcp.stopListening(dv);
    }

    private void stopSftpServer(){
        ShareModule.stopSftpServer(); // maybe you need do this something different
    }


    private void removeClipBoardListener() {
        ClipBoard.removeListener(getApplicationContext());
    }

    private void startClipboardListener() {
        ClipBoard.setListener(this);
    }

    private void unregisterBatteryRecever() {
        if(batteryRegistered)
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
            unregisterBatteryRecever();
            removeClipBoardListener();
        } else {
            MainActivity.setSelected_id(Device.getConnectedDevices().values().iterator().next().getId());
        }
    }

    public static void addCommandToQueue(Context context,BackgroundServiceCmds cmd){
        Intent intent = new Intent(context, BackgroundService.class);
        intent.putExtra("cmd",cmd.name());
        intent.setAction("cmd");
        context.startService(intent);
    }

    public static void submitNewTask(Context context,Runnable task){ // todo this for timer
        Intent intent = new Intent(context, BackgroundService.class);
        takQueue.add(task);
        intent.setAction("task");
        context.startService(intent);
    }

}
