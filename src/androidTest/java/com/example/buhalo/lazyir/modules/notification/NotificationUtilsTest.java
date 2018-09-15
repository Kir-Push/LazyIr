package com.example.buhalo.lazyir.modules.notification;

import android.app.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;

import com.example.buhalo.lazyir.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by buhalo on 26.11.17.
 */
@RunWith(Parameterized.class)
public class NotificationUtilsTest {

    private NotificationManager notificationManager;
    private   Context appContext;
    private  android.app.Notification notification;
    private  android.app.Notification notification2;
    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getTargetContext();
        notificationManager = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(appContext)
                        .setSmallIcon(R.drawable.delete48)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        notification = mBuilder.build();
        notification2 =   new NotificationCompat.Builder(appContext)
                        .setSmallIcon(R.drawable.btn_close)
                        .setContentTitle("My notification-2")
                        .setContentText("Hello World-2!").build();
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[20][0]);
    }


    @Test
    public void castToMyNotification() throws Exception {

        notificationManager.cancelAll();
        Thread.sleep(10);
        notificationManager.notify(1,notification);
        Icon smallIcon = notification.getSmallIcon();
        Drawable drawable = smallIcon.loadDrawable(appContext);
        Bitmap bitmap = drawableToBitmap(drawable);
        String base64 = bitmapToBase64(bitmap);
        String base642 = bitmapToBase64(drawableToBitmap(notification2.getSmallIcon().loadDrawable(appContext)));
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        com.example.buhalo.lazyir.modules.notification.notifications.Notification notification1 = NotificationUtils.castToMyNotification(activeNotifications[0]);
        assertEquals(activeNotifications.length,1);
        System.out.println(notification1.getIcon());
        assertEquals(notification1.getText(),"Hello World!");
        assertEquals(notification1.getTitle(),"My notification");
        assertNotEquals(notification1.getIcon(),base642);



        notificationManager.notify(2,notification2);
        Thread.sleep(10);
        activeNotifications = notificationManager.getActiveNotifications();
        com.example.buhalo.lazyir.modules.notification.notifications.Notification notification3 = NotificationUtils.castToMyNotification(activeNotifications[1].getId() != 2 ? activeNotifications[0] : activeNotifications[1]);
        System.out.println(notification3.getIcon());
        assertNotEquals(notification3.getIcon(),base64);
        assertEquals(notification3.getIcon(),base642);
        assertEquals(notification3.getTitle(),"My notification-2");
        assertEquals(notification3.getText(),"Hello World-2!");
        assertNotEquals(notification3.getText(),"Hello World");
        assertNotEquals(activeNotifications[0].getId(),activeNotifications[1].getId());
        // todo rewrite, невозможно читать
    }

    private static String bitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if(bitmap == null)
            return null;

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    //https://stackoverflow.com/questions/37252119/how-to-convert-a-icon-to-bitmap-in-android    --- drawable to bitmap
    private static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        return bitmap;
    }


}