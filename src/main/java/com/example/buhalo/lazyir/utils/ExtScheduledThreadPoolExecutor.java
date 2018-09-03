package com.example.buhalo.lazyir.utils;

import android.util.Log;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExtScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private static final String TAG = "ThreadPoolExecutor";
    public ExtScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if(t instanceof Exception){
            Log.e(TAG,"In some thread error: ", t);
        }
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> scheduledFuture = super.scheduleAtFixedRate(command, initialDelay, period, unit);
        if(command instanceof ScheludeRunnable) {
            ((ScheludeRunnable) command).setFuture(scheduledFuture);
        }
        return scheduledFuture;
    }

    abstract static class ScheludeRunnable implements Runnable{
        ScheduledFuture<?> myFuture;
        void setFuture(ScheduledFuture<?> future) {
            this.myFuture = future;
        }
    }
}