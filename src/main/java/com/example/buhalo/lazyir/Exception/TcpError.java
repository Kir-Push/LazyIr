package com.example.buhalo.lazyir.Exception;

/**
 * Created by buhalo on 28.02.17.
 */
public class TcpError extends Exception {
    public TcpError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TcpError(String detailMessage) {
        super(detailMessage);
    }
}
