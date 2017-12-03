package com.example.buhalo.lazyir.modules.notificationModule.notifications;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.io.ByteArrayOutputStream;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

/**
 * Created by buhalo on 26.11.17.
 */

 public class NotificationUtils {
    // todo reverse notifs for image and text carefully
    public static Notification castToMyNotification(StatusBarNotification sbn)
    {
        String pack = sbn.getPackageName();
        String text = extractText(sbn);
        String title = extractTitle(sbn);
        String ticker = extractTicker(sbn);
        String icon = extractIcon(sbn);
        String picture = extractImage(sbn);
        if(pack.equals("com.whatsapp") && text == null)
            return null;
        // telegram send two notifs, first notif with new message title contain action, second not. Skip if new message non exist
//        if(pack.equals("org.telegram.messenger") && !title.contains("(\d new message)"))
        return  new Notification(text,title,pack,ticker, NetworkPackage.getMyId(),icon,picture);
    }

    private static String extractTicker(StatusBarNotification sbn) {
        String result = null;
        CharSequence tickerText = sbn.getNotification().tickerText;
        if(tickerText != null)
            result = tickerText.toString();
        return result;
    }

    private static String extractTitle(StatusBarNotification sbn) {
        String result = null;
        Bundle extras = sbn.getNotification().extras;
        if(extras == null)
            return null;
        CharSequence bigTitle = (CharSequence) extras.getCharSequence(android.app.Notification.EXTRA_TITLE_BIG);
        if(bigTitle != null)
            result = bigTitle.toString();
        if(result == null || result.length() <= 0){
            CharSequence title = (CharSequence) extras.getCharSequence(android.app.Notification.EXTRA_TITLE);
            if(title != null)
                result = title.toString();
        }
        return result;
    }

    private static String extractText(StatusBarNotification sbn){
        String result = null;
        Bundle extras = sbn.getNotification().extras;
        if(extras == null)
            return null;
        // first try extract big text
        CharSequence[] lines = extras.getCharSequenceArray(android.app.Notification.EXTRA_TEXT_LINES);
        if(lines != null && lines.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (CharSequence msg : lines)
                if (!TextUtils.isEmpty(msg)) {
                    sb.append(msg.toString());
                    sb.append('\n');
                }
            result = sb.toString().trim();
        }

        if(result == null || result.length() <= 0){
            CharSequence chars = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT);
            if(!TextUtils.isEmpty(chars))
                result = chars.toString();
        }
        // if fail's extract simple text
        if(result == null || result.length() <= 0){
            result = extras.getString(android.app.Notification.EXTRA_TEXT);
        }
        return result;
    }


    //todo  EXTRA_SUB_TEXT create see Notification class

    private static String extractIcon(StatusBarNotification sbn) {
        android.app.Notification notification = sbn.getNotification();
        Icon icon = (icon = notification.getLargeIcon()) == null ? notification.getSmallIcon() : icon;
        if(icon == null)
            return null;
        try {
            Context packageContext = BackgroundService.getAppContext().createPackageContext(sbn.getPackageName(), CONTEXT_IGNORE_SECURITY);
            Drawable drawable = icon.loadDrawable(packageContext);
            Bitmap bitmap = drawableToBitmap(drawable);
            return bitmapToBase64(bitmap);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("NotificationListener","getIcon",e);
            return  null;
        }
    }
    //https://stackoverflow.com/questions/40325307/how-to-get-an-image-from-another-apps-notification
    private static String extractImage(StatusBarNotification sbn){
        Bundle bundle = sbn.getNotification().extras;
        if(bundle == null || bundle.containsKey(android.app.Notification.EXTRA_PICTURE))
            return null;
        Bitmap bmp = (Bitmap) bundle.get(android.app.Notification.EXTRA_PICTURE);
        return bitmapToBase64(bmp);
    }

    public static String bitmapToBase64(Bitmap bitmap){
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
