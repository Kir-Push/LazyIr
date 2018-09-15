package com.example.buhalo.lazyir.service.network.tcp;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.service.BackgroundUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;



public class TcpConnectionManager {
    private static final String TAG = "TcpConnectionManager";
    public enum api {
        TCP,
        INTRODUCE,
        PING,
        PAIR_RESULT,
        RESULT,
        OK,
        REFUSE,
        PAIR,
        UNPAIR,
        SYNC,
        ENABLED_MODULES
    }

    private MessageFactory messageFactory;
    private ModuleFactory moduleFactory;
    private PairService pairService;
    private DBHelper dbHelper;

    @Inject
    public TcpConnectionManager(MessageFactory messageFactory, ModuleFactory moduleFactory, PairService pairService,DBHelper dbHelper) {
        this.messageFactory = messageFactory;
        this.moduleFactory = moduleFactory;
        this.pairService = pairService;
        this.dbHelper = dbHelper;
    }

    // configure sslsocket for tls connection
    private Socket getConnection(InetAddress ip, int port,Context context) throws IOException  {
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.testkeys);
            trustStore.load(trustStoreStream, "bimkaSamokat".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLSocketFactory factory = sslContext.getSocketFactory();
            return factory.createSocket(ip, port);
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Exception while creating connection: ", e);
            throw new IOException("Could not connect to SSL Server", e);
        }
    }

     public void receivedUdpIntroduce(InetAddress address, int port, NetworkPackage np, Context context) {
        try {
            // at this moment connect only to pc
            if(!np.getDeviceType().equals("pc")) {
                return;
            }
            Socket socket = getConnection(address,port,context);
            // submit connection to executorService - this service is main for app
            BackgroundUtil.submitTask(new ConnectionThread(socket, context,messageFactory,moduleFactory,pairService,dbHelper),context);
        } catch (IOException e) {
            Log.e(TAG,"Exception on accept connection ignoring ",e);
        }
    }

    public void stopListening(Device closingDevice) {
        if(closingDevice != null) {
            closingDevice.closeConnection();
        }
    }


}
