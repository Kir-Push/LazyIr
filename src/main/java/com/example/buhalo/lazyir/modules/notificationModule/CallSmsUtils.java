package com.example.buhalo.lazyir.modules.notificationModule;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import com.example.buhalo.lazyir.modules.notificationModule.notifications.NotificationUtils;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by buhalo on 30.11.17.
 */

public class CallSmsUtils {

    public static String getContactImage(Context context,final String contactNumber){
        if(contactNumber == null)
            return null;
        String result = null;
        long idFromContact = getContactIDFromNumber(contactNumber,context);
        InputStream is = openDisplayPhoto(idFromContact);
        if(is == null)
            is = openPhoto(idFromContact); // if < 1 or 0 error illegalacces /todo
        Bitmap bitmap = BitmapFactory.decodeStream(is); // todo test decodeStream
        return NotificationUtils.bitmapToBase64(bitmap);
    }


//    private static long getIdFromContact(Context context, final String addr) {
//        String id;
//        Uri personUri = Uri.withAppendedPath(
//                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, addr);
//        Cursor cur = context.getContentResolver().query(personUri,
//                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
//                null, null, null);
//        try {
//            if (cur != null && cur.moveToFirst()) {
//                id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
//                return Long.valueOf(id);
//            }
//        }finally {
//            if (cur != null)
//                cur.close();
//        }
//        return -1;
//    }

    public static int getContactIDFromNumber(String contactNumber,Context context)
    {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = new Random().nextInt();
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,contactNumber),new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        while(contactLookupCursor.moveToNext()){
            phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        contactLookupCursor.close();

        return phoneContactID;
    }

    private static InputStream openDisplayPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd = BackgroundService.getAppContext().getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd != null ? fd.createInputStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static InputStream openPhoto(long contactId) {
        try {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            return ContactsContract.Contacts.openContactPhotoInputStream(
                    BackgroundService.getAppContext().getContentResolver(), contactUri);
        }
        catch (IllegalArgumentException e){
            return null;
        }
//        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
//        Cursor cursor =  BackgroundService.getAppContext().getContentResolver().query(photoUri,
//                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
//        if (cursor == null) {
//            return null;
//        }
//        try {
//            if (cursor.moveToFirst()) {
//                byte[] data = cursor.getBlob(0);
//                if (data != null) {
//                    return new ByteArrayInputStream(data);
//                }
//            }
//        } finally {
//            cursor.close();
//        }
//        return null;
    }

    public static String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.Contacts.DISPLAY_NAME+" like '" + name +"'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        if (c != null && c.moveToFirst()) {
            ret = c.getString(0);
        }
        if (c != null) {
            c.close();
        }
        if(ret==null)
            ret = "Unsaved";
        return ret;
    }


    public static String getName(String number, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER+" like " + number +"";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor c = context.getContentResolver().query(contactUri,
                projection, null, null, null);
        if (c != null && c.moveToFirst()) {
            ret = c.getString(0);
        }
        if (c != null) {
            c.close();
        }
        if(ret==null)
            ret = number;
        return ret;
    }
}
