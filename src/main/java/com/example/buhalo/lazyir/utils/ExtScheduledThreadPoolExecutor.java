package com.example.buhalo.lazyir.utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ExtScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public ExtScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public ExtScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        //todo handle exceptions!!
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> scheduledFuture = super.scheduleAtFixedRate(command, initialDelay, period, unit);
        if(command instanceof ScheludeRunnable)
        {
            ((ScheludeRunnable) command).setFuture(scheduledFuture);
        }
        return scheduledFuture;
    }

    public static abstract class ScheludeRunnable implements Runnable{

        public void setFuture(ScheduledFuture<?> future) {
            this.myFuture = future;
        }

        public ScheduledFuture<?> myFuture;


    }
}