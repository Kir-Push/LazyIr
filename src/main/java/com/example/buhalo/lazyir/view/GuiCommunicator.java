package com.example.buhalo.lazyir.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.buhalo.lazyir.R;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.service.dto.TcpDto;
import com.example.buhalo.lazyir.service.receivers.NotifActionReceiver;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GuiCommunicator {

    private static final String CHANEL_PAIR_ID = "775533";
    private static final int PAIR_ID = 775533;

    public void requestPair(NetworkPackage np, Context context) { //create notification for pairing
        TcpDto dto = (TcpDto) np.getData();
        String data = dto.getData();
        Intent yesAction = new Intent(context,NotifActionReceiver.class);
        yesAction.setAction("Yes");
        yesAction.putExtra("id",PAIR_ID);
        yesAction.putExtra("user-id",np.getId());
        yesAction.putExtra("value",data);
        yesAction.putExtra("command","pairAnswer");
        Intent noReceive = new Intent(context,NotifActionReceiver.class);
        noReceive.setAction("No");
        noReceive.putExtra("id",PAIR_ID);
        noReceive.putExtra("user-id",np.getId());
        noReceive.putExtra("value",data);
        noReceive.putExtra("command","pairAnswer");
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 775533, yesAction, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(context, 775534, noReceive, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,CHANEL_PAIR_ID)
                        .setSmallIcon(R.mipmap.no_pair)
                        .setContentTitle(np.getName() + " Request pairing!")
                        .setContentText("You want to be friend?");
        mBuilder.addAction(R.mipmap.yes_pair,"Yes",pendingIntentYes);
        mBuilder.addAction(R.drawable.delete48,"No",pendingIntentNo);
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if(mNotifyMgr != null) {
            mNotifyMgr.notify(PAIR_ID, mBuilder.build());
        }
    }

}
