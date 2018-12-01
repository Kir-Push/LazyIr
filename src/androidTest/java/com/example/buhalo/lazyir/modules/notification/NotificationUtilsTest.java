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