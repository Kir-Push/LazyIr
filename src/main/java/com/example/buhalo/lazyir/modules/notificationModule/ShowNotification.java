package com.example.buhalo.lazyir.modules.notificationModule;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.NotificationListener;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

/**
 * Created by buhalo on 20.04.17.
 */

public class ShowNotification extends Module {
    public static final String SHOW_NOTIFICATION = "ShowNotification";

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals("ALL NOTIFS"))
        {
            Notifications notifications = new Notifications();
            String ns = Context.NOTIFICATION_SERVICE;
            StatusBarNotification[] activeNotifications =NotificationListener.getAll();
            if(activeNotifications == null)
            {
                return;
            }
            for(int i = 0;i<activeNotifications.length;i++)
            {
      //          System.out.println("\n\n\n\n\n\n\n\n" + activeNotifications[i].);
                Notification notification =  castNotf(activeNotifications[i]);
                notifications.addNotification(notification);
            }
            NetworkPackage nps = new NetworkPackage(SHOW_NOTIFICATION,"ALL NOTIFS");
            nps.setObject(NetworkPackage.N_OBJECT,notifications);
            TcpConnectionManager.getInstance().sendCommandToServer(device.getId(),nps.getMessage());
        }
    }

    private Notification castNotf(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();
        String text = "";
        String title;
        String ticker = "";
        Bundle extras = sbn.getNotification().extras;
        String txt = tryExtract(extras);
        CharSequence charSequence = extras.getCharSequence("android.text");
        CharSequence tickerText = sbn.getNotification().tickerText;
        if(charSequence != null)
        {
            text = charSequence.toString();
        }
        if(txt != null)
        {
            text = txt;
        }
        if(tickerText != null)
        {
            ticker = tickerText.toString();
        }
        try {

            title = extras.getString("android.title");
            if(title == null)
            {
                title = ((SpannableString)extras.get("android.title")).toString();
            }
            if(title == null)
            {
                title = "";
            }
        }
       catch (Exception e)
       {
           title = "";
       }
//                ticker = tickerText.toString();
//                text = charSequence.toString();
        Notification notification = new Notification(text,title,pack,ticker, Build.SERIAL);
        Log.i(SHOW_NOTIFICATION,pack + "  " + title + "  " + text + "  " + ticker);
        return notification;
    }
    private String tryExtract(Bundle extras)
    {
        CharSequence[] lines =
                extras.getCharSequenceArray(android.app.Notification.EXTRA_TEXT_LINES);
        if(lines != null && lines.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (CharSequence msg : lines)
                if (!TextUtils.isEmpty(msg)) {
                    sb.append(msg.toString());
                    sb.append('\n');
                }
            return sb.toString().trim();
        }
        CharSequence chars =
                extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT);
        if(!TextUtils.isEmpty(chars))
            return chars.toString();
        return null;
    }
}
