package com.example.buhalo.lazyir.modules.shareScreen;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.modules.shareScreen.enity.AuthInfo;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ScreenShareModule extends Module {
    private static final String REGISTER = "register";
    private static final String UNREGISTER = "unregister";
    private static final String TOKEN = "token";
    private BlockingQueue<byte[]> queue;
    private BlockingQueue<Integer> commandQueue;
    private Future<?> receiverFuture;
    private boolean receiverWork;
    private String token;
    private int port;

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data){
            case TOKEN:
                startConnection(np);
                break;
        }
    }

    private void startConnection(NetworkPackage np) {
        AuthInfo auth = np.getObject(TOKEN, AuthInfo.class);
        token = auth.getToken();
        port = auth.getPort();
        queue = new LinkedBlockingQueue<>();
        commandQueue = new LinkedBlockingQueue<>();
        if(receiverFuture != null && !receiverFuture.isDone() && !receiverFuture.isCancelled())
            receiverFuture.cancel(true);
        receiverFuture = BackgroundService.getExecutorService().submit(new ImageReceiver());
        receiverWork = true;

    }


    public void register(){
        sendMsg(NetworkPackage.Cacher.getOrCreatePackage(ScreenShareModule.class.getSimpleName(), REGISTER).getMessage());
    }

    public void unRegister(){
        sendMsg(NetworkPackage.Cacher.getOrCreatePackage(ScreenShareModule.class.getSimpleName(), UNREGISTER).getMessage());
        receiverFuture.cancel(true);
        receiverWork = false;
    }


    class ImageReceiver implements Runnable{

        @Override
        public void run() {
            try {
                Socket socket = new Socket(device.getIp(),port);
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeBytes(device.getId());
                outputStream.writeBytes(token);
                while (receiverWork){
                    int command = inputStream.readInt();
                    if(command == 1) fullImage(inputStream);
                    else if(command == 2) partImage(inputStream);
                }
            } catch (IOException | InterruptedException  e) {
                e.printStackTrace();
            }

        }

        private void partImage(DataInputStream inputStream) throws IOException, InterruptedException {
            int longX = inputStream.readInt();
            int startX = inputStream.readInt();
            int startY = inputStream.readInt();
            int length = inputStream.readInt();
            byte[] bytes = new byte[length];
            inputStream.read(bytes,0,length);
            commandQueue.offer(2,50,TimeUnit.MILLISECONDS);
            commandQueue.offer(longX,50,TimeUnit.MILLISECONDS);
            commandQueue.offer(startX,50,TimeUnit.MILLISECONDS);
            commandQueue.offer(startY,50,TimeUnit.MILLISECONDS);
            queue.offer(bytes,50, TimeUnit.MILLISECONDS);

        }

        private void fullImage(DataInputStream inputStream) throws IOException, InterruptedException {
            int length = inputStream.readInt();
            byte[] bytes = new byte[length];
            inputStream.read(bytes,0,length);
            commandQueue.offer(1,50,TimeUnit.MILLISECONDS);
            queue.offer(bytes,50, TimeUnit.MILLISECONDS);
        }
    }
}
