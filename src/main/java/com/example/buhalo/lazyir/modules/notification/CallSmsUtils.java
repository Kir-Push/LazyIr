package com.example.buhalo.lazyir.modules.notification;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.inject.Inject;


public class CallSmsUtils {
    private static final String TAG = "CallSmsUtils";
    private Context context;
    private NotificationUtils utils;

    @Inject
    public CallSmsUtils(Context context, NotificationUtils utils) {
        this.context = context;
        this.utils = utils;
    }

    public String getContactImage(Context context, final String contactNumber){
        if (contactNumber == null) {
            return null;
        }
        long idFromContact = getContactIDFromNumber(contactNumber, context);
        InputStream is = openDisplayPhoto(idFromContact);
        if (is == null) {
            is = openPhoto(idFromContact);
        }
        if (is == null) {
            return "";
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        try {
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "error in getContactImage", e);
        }
        return utils.bitmapToBase64(bitmap);
    }


    private int getContactIDFromNumber(String contactNumber,Context context) {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = new Random().nextInt();
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,contactNumber),new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        if(contactLookupCursor == null){
            return 0;
        }
        while(contactLookupCursor.moveToNext()){
            phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        contactLookupCursor.close();
        return phoneContactID;
    }

    private InputStream openDisplayPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd != null ? fd.createInputStream() : null;
        } catch (IOException e) {
            Log.e(TAG,"error in openDisplayPhoto",e);
        }
        return null;
    }

    private InputStream openPhoto(long contactId) {
        try {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            return ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri);
        } catch (IllegalArgumentException e){
            Log.e(TAG,"error in openPhoto",e);
        }
        return null;
    }

    public String getPhoneNumber(String name) {
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
        if(ret==null) {
            ret = "Unsaved";
        }
        return ret;
    }


    public String getName(String number, Context context) {
        String ret = null;
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor c = context.getContentResolver().query(contactUri, projection, null, null, null);
        if (c != null && c.moveToFirst()) {
            ret = c.getString(0);
        }
        if (c != null) {
            c.close();
        }
        if(ret==null) {
            ret = number;
        }
        return ret;
    }
}
