package com.example.buhalo.lazyir.service;

/**
 * Created by buhalo on 15.11.17.
 */

public enum BackgroundServiceCmds {
    startUdpListener,
    stopUdpListener,
    stopTcpListener,
    startListeningTcp,
    startSendPeriodicallyUdp,
    stopSendingPeriodicallyUdp,
    closeAllTcpConnections,
    stopSftpServer,
    removeClipBoardListener,
    startClipboardListener,
    unregisterBatteryRecever,
    registerBatteryReceiver,
    onZeroConnections,
    submitTask
}
