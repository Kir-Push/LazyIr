package com.example.buhalo.lazyir.modules.notification;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.provider.Telephony;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.modules.notification.notifications.Notification;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;
import static com.example.buhalo.lazyir.service.listeners.NotificationListener.SMS_TYPE;
import static com.example.buhalo.lazyir.service.listeners.NotificationListener.SMS_TYPE_2;


 public class NotificationUtils {
     private static final String TAG = "NotificationListener";

     @Getter @Setter
     private ConcurrentHashMap<String,StatusBarNotification> pendingNotifs = new ConcurrentHashMap<>();
     private Context context;

     @Inject
     public NotificationUtils(Context context) {
         this.context = context;
     }

    public Notification castToMyNotification(StatusBarNotification sbn) {
        Notification notification = new Notification(Integer.toString(sbn.getId()));
        notification.setPack(sbn.getPackageName());
        notification.setText(extractText(sbn));
        notification.setTitle(extractTitle(sbn));
        notification.setTicker(extractTicker(sbn));
        notification.setIcon(extractIcon(sbn));
        notification.setPicture(extractImage(sbn));
        notification.setType(extractType(sbn,sbn.getPackageName(),notification.getTitle(),notification.getText()));
        return notification;
    }

     public boolean isSms(Notification notification) {
        String pack = notification.getPack();
        return pack != null && (pack.equals(SMS_TYPE) || pack.equals(SMS_TYPE_2) || pack.equals(Telephony.Sms.getDefaultSmsPackage(context)));
    }

    private String extractType(StatusBarNotification sbn,String pack,String title,String text){
        if(pack != null && (pack.equals(SMS_TYPE) || pack.equals(SMS_TYPE_2) || pack.equals(Telephony.Sms.getDefaultSmsPackage(context)))) { //todo проверь, если скажем fb messenger стоит стандартным sms, не считает ли он все его сообщение смс, если да то свенряйся с smsbroadcast, если туда приходило значит sms, если нет, то нет
            return NotificationTypes.SMS.name();
        }
        else if(pack != null && messengersMessage(sbn,pack,title,text)) {
            return NotificationTypes.MESSENGER.name();
        }
        return NotificationTypes.NOTIFICATION.name();
    }

    // i here add new methods after testing change old to new
    private boolean messengersMessage(StatusBarNotification sbn,String pack,String title,String text) {
        android.app.Notification notification = sbn.getNotification();
        if(notification == null){
            return false;
        }
        Bundle bundle = notification.extras;
        if(pack == null || pack.equals("com.google.android.googlequicksearchbox") || (pack.equals("com.whatsapp") && text == null)) {
            return false;
        }
        Optional<String> first = Stream.of(bundle.keySet())
                .filter(key -> key.equals("android.wearable.EXTENSIONS"))// telegram send second notif with id 1, it not contain action, therefore ignore it
                .findFirst();
        if(first.isPresent() && ((sbn.getId() > 1 && pack.equals("org.telegram.messenger")) || !pack.equals("org.telegram.messenger"))){
            pendingNotifs.put(pack + ":" + title,sbn);
            return true;
        }
        return false;
    }


    private String extractTicker(StatusBarNotification sbn) {
        String result = null;
        android.app.Notification notification = sbn.getNotification();
        if(notification == null){
            return "";
        }
        CharSequence tickerText = notification.tickerText;
        if(tickerText != null) {
            result = tickerText.toString();
        }
        return result;
    }

    public String extractTitle(StatusBarNotification sbn) {
        String result = null;
        android.app.Notification notification = sbn.getNotification();
        if(notification == null){
            return "";
        }
        Bundle extras = notification.extras;
        if(extras == null) {
            return null;
        }
        CharSequence bigTitle = extras.getCharSequence(android.app.Notification.EXTRA_TITLE_BIG);
        if(bigTitle != null) {
            result = bigTitle.toString();
        }
        if(result == null || result.length() <= 0){
            CharSequence title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE);
            if(title != null) {
                result = title.toString();
            }
        }
        return result;
    }

    private String extractText(StatusBarNotification sbn){
        String result = null;
        android.app.Notification notification = sbn.getNotification();
        if(notification == null){
            return "";
        }
        Bundle extras = notification.extras;
        if(extras == null) {
            return null;
        }
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
            Object obj = extras.get(android.app.Notification.EXTRA_TEXT);
            if(obj != null) {
                result = obj.toString();
            }
        }
        return result;
    }

    private String extractIcon(StatusBarNotification sbn) {
        android.app.Notification notification = sbn.getNotification();
        if(notification == null){
            return null;
        }
        boolean smallIcon = true;
        if(notification.getLargeIcon() != null) {
            smallIcon = false;
        }
        Icon icon = (icon = notification.getLargeIcon()) == null ? notification.getSmallIcon() : icon;
        if(icon == null) {
            return null;
        }
        try {
            Context packageContext = context.createPackageContext(sbn.getPackageName(), CONTEXT_IGNORE_SECURITY);
            Drawable drawable = icon.loadDrawable(packageContext);
            Bitmap bitmap = drawableToBitmap(drawable,smallIcon);
            return bitmapToBase64(bitmap);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,"error in extractIcon",e);
            return  null;
        }
    }
    //https://stackoverflow.com/questions/40325307/how-to-get-an-image-from-another-apps-notification
    private String extractImage(StatusBarNotification sbn){
        android.app.Notification notification = sbn.getNotification();
        if(notification == null){
            return null;
        }
        Bundle bundle = notification.extras;
        if(bundle == null || !bundle.containsKey(android.app.Notification.EXTRA_PICTURE)) {
            return null;
        }
        Bitmap bmp = (Bitmap) bundle.get(android.app.Notification.EXTRA_PICTURE);
        return bitmapToBase64(bmp);
    }

    public String bitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if(bitmap == null) {
            return null;
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    //https://stackoverflow.com/questions/37252119/how-to-convert-a-icon-to-bitmap-in-android    --- drawable to bitmap
    private Bitmap drawableToBitmap (Drawable drawable,boolean smallIcon) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap1 = bitmapDrawable.getBitmap();
            if (bitmap1 != null) {
                if(smallIcon) {
                    Paint paint = new Paint();
                    int[] colors = {android.R.color.darker_gray, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_orange_dark,
                            android.R.color.holo_purple,android.R.color.holo_green_dark,android.R.color.holo_blue_dark,android.R.color.holo_orange_light,android.R.color.holo_red_light};
                    ColorFilter filter = new PorterDuffColorFilter(ContextCompat.getColor(context, colors[new Random().nextInt(colors.length)]), PorterDuff.Mode.SRC_IN);
                    paint.setColorFilter(filter);
                    Bitmap mutableBitmap = bitmap1.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(mutableBitmap);
                    canvas.drawBitmap(mutableBitmap, 0, 0, paint);
                    return mutableBitmap;
                }
                return bitmap1;
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
