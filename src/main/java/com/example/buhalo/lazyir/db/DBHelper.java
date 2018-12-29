package com.example.buhalo.lazyir.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;

import com.example.buhalo.lazyir.modules.clipboard.ClipboardDB;
import com.example.buhalo.lazyir.modules.notification.CallSmsUtils;
import com.example.buhalo.lazyir.modules.sendcommand.Command;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.modules.notification.sms.Sms;
import com.example.buhalo.lazyir.modules.notification.reminder.MissedCall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Synchronized;



public class DBHelper extends SQLiteOpenHelper implements DbCommands {

    private static final String TAG = "DBHelper";
    private static final int DATABASE_VERSION = 35;
    private static final String LIKE_AND = " LIKE ? AND ";
    private static final String LIKE = " LIKE ?";
    private String dbpath;
    private static final String DATABASE_NAME = "Buttons.db";
    private Context context;
    private CallSmsUtils callSmsUtils;

    @Inject
    public DBHelper(CallSmsUtils callSmsUtils,Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbpath = "/data/data/" + context.getApplicationContext().getPackageName() + "/databases/";
        this.context = context;
        this.callSmsUtils = callSmsUtils;
        createDataBase(context);
    }

    //************************************************************************************************************
    // Utility method's
    // checking whether database file - lie in folder.
    private boolean checkDataBase() {
        File dbFile = new File(dbpath + DATABASE_NAME);
        return dbFile.exists();
    }

    // copy database from file in project(default, init DB file), which will be in asset's folder, to
    // filer specified in DATABASE_NAME const. Which are App db file
    private void copyDataBase(Context context) throws IOException {
        String outFileName = dbpath + DATABASE_NAME;
        try(OutputStream mOutput = new FileOutputStream(outFileName);
            InputStream mInput = context.getApplicationContext().getAssets().open(DATABASE_NAME)) {
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = mInput.read(mBuffer)) > 0) {
                mOutput.write(mBuffer, 0, mLength);
            }
            mOutput.flush();
        }
    }


    private void createDataBase(Context context) {
        //If the database does not exist, copy it from the assets.
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {     //Copy the database from assests
                copyDataBase(context);
                Log.d(TAG, "createDatabase database created");
            } catch (IOException mIOException) {
                Log.e(TAG, "createDatabase error");
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //hz
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    //************************************************************************************************************
    // Pairing action's - getCode and CRUD's
    public void savePairedDevice(String dvId, String pairCode) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_DVID, dvId);
            values.put(COLUMN_NAME_PAIRCODE, pairCode);
            db.insert(TABLE_NAME_PAIRED_DEVICES, null, values);
        }
    }

    public void deletePaired(String dvId) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selection = COLUMN_NAME_DVID + LIKE;
            String[] args = new String[]{dvId};
            db.delete(TABLE_NAME_PAIRED_DEVICES, selection, args);
        }
    }

    public String getPairedCode(String dvId) {
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {COLUMN_NAME_PAIRCODE};
            String selection = COLUMN_NAME_DVID + LIKE;
            String[] args = new String[]{dvId};
            try (Cursor c = db.query(TABLE_NAME_PAIRED_DEVICES, projection, selection, args, null, null, null)) {
               if(c.moveToNext()){
                    return c.getString(c.getColumnIndex(COLUMN_NAME_PAIRCODE));
                }
            }
        }
        return null;
    }
    //************************************************************************************************************
    // Clipboard Commands
    public List<ClipboardDB> getClipboardFull() {
        List<ClipboardDB> clipboards = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    _ID,
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_OWNER
            };
            try (Cursor c = db.query(TABLE_NAME_CLIPBOARD, projection, null, null, null, null, null)) {
                while (c.moveToNext()) {
                    ClipboardDB clipboard = new ClipboardDB(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)), c.getString(c.getColumnIndex(COLUMN_NAME_OWNER)), c.getInt(c.getColumnIndex(_ID)));
                    clipboards.add(clipboard);
                }
            }
        }
        return clipboards;
    }

    public void clearAllClipboard(){
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.delete(TABLE_NAME_CLIPBOARD, null, null);
        }
    }

    public void deleteClipboard(ClipboardDB clipboardDB){
        deleteClipboard(clipboardDB.getId());
    }

    private void deleteClipboard(int id){
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selection = _ID + LIKE;
            String[] selectionArgs = {String.valueOf(id)};
            db.delete(TABLE_NAME_CLIPBOARD, selection, selectionArgs);
        }
    }

    public void saveClipboard(ClipboardDB clipboardDB){
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_TEXT, clipboardDB.getText());
            values.put(COLUMN_NAME_OWNER, clipboardDB.getOwner());
            db.insert(TABLE_NAME_CLIPBOARD, null, values);
        }
        while(getCliboardCount() > 20){
            try (SQLiteDatabase db = getWritableDatabase()) {
                Cursor c = db.query(TABLE_NAME_CLIPBOARD, new String[] { "min(" + _ID + ")" }, null, null,
                        null, null, null);
                c.moveToFirst();
                int rowID = c.getInt(0);
                deleteClipboard(rowID);
            }
        }
    }

    public long getCliboardCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME_CLIPBOARD);
        db.close();
        return count;
    }

    //************************************************************************************************************
    // Global commands CRUD action's and
    public void saveCommand(Command command) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_TEXT, command.getCommandName());
            values.put(COLUMN_NAME_COMMAND, command.getCmd());
            db.insert(TABLE_NAME_COMMANDS, null, values);
        }
    }

    public List<Command> getCommand(String commandName) {
        List<Command> commands = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    _ID,
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMMAND
            };
            String selection = null;
            String[] selectionArgs = null;
            if (commandName != null) {
                selection = COLUMN_NAME_TEXT + LIKE;
                selectionArgs = new String[]{commandName};
            }
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    Command command = new Command(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), c.getInt(c.getColumnIndex(_ID)));
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    public int updateCommand(Command command){
        try(SQLiteDatabase db = getWritableDatabase()){
            ContentValues values = new ContentValues();
            String selection = _ID + LIKE;
            String[] selectionArgs = {String.valueOf(command.getId())};
            values.put(_ID,command.getId());
            values.put(COLUMN_NAME_COMMAND,command.getCmd());
            values.put(COLUMN_NAME_TEXT,command.getCommandName());
            return db.update(TABLE_NAME_COMMANDS, values, selection,selectionArgs);
        }
    }

    public List<Command> getCommandFull() {
        List<Command> commands = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    _ID,
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMMAND
            };
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, null, null, null, null, null)) {
                while (c.moveToNext()) {
                    Command command = new Command(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), c.getInt(c.getColumnIndex(_ID)));
                    commands.add(command);
                }
            }
        }
        return commands;
    }
    // delete command from DB
    public void deleteCommand(Command command) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selection = _ID + LIKE;
            String[] selectionArgs = {String.valueOf(command.getId())};
            db.delete(TABLE_NAME_COMMANDS, selection, selectionArgs);
        }
    }
    //************************************************************************************************************
    // Modules Info methods - get enabled, filling defaults, enabling and disabling and so on.
    // common variables for modules methods
    private String[] moduleProjectionSimple = {
            COLUMN_NAME_MODULE_NAME,
            COLUMN_NAME_MODULE_DEVICE,
            COLUMN_NAME_MODULE_STATUS};
    private String statusOn = "ON";
    private String statusOff = "OFF";
    // first try getting enabledModulesBy device, if size 0, check if device non exist in db(first time connect, if true then fill by default list of enabled modules(all).
    // return enabledModules in each cases
    public List<String> checkAndSetDefaultIfNoInfo(String dv) {
        if (dv == null) {
            return new ArrayList<>();
        }
        List<String> enabledModules = getEnabledModules(dv);
        if (enabledModules.isEmpty() && checkIfFirstTimeDevice(dv)) {
             fillStandart(dv);
            return getEnabledModules(dv);
        }
        return enabledModules;
    }
    // populate module table for default enabled modules settings
    // it's called when device connect for the fisrt time,and no setting's for device in db
    // return list of enabled modules names by this method - actually all modules
    // lock for write and read
    @Synchronized
    private void fillStandart(String dv) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            for (com.example.buhalo.lazyir.utils.entity.Pair<Class, Class> pair : ModuleFactory.getRegisteredModules().values()) {
                Class moduleClass = pair.getLeft();
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_MODULE_NAME, moduleClass.getSimpleName());
                values.put(COLUMN_NAME_MODULE_DEVICE, dv);
                values.put(COLUMN_NAME_MODULE_STATUS, statusOn);
                db.insert(TABLE_NAME_MODULE, null, values);
            }
        }
    }
    // checking if device modules info exist in db.
    // simply checking module table for device id.
    // lock for write but not for read.
    // return true if non exist, otherwise false.
    @Synchronized
    private boolean checkIfFirstTimeDevice(String dv) {
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {COLUMN_NAME_MODULE_DEVICE};
            String selection = COLUMN_NAME_MODULE_DEVICE + LIKE;
            String[] selectionArgs = {dv};
            try (Cursor c = db.query(TABLE_NAME_MODULE, projection, selection, selectionArgs, null, null, null)) {
                return c.getCount() <= 0;
            }
        }
    }


    @Synchronized
    private List<String> getEnabledModules(String dv) {
        List<String> result = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String selection = COLUMN_NAME_MODULE_DEVICE + LIKE_AND + COLUMN_NAME_MODULE_STATUS + LIKE;
            String[] selectionArgs = {dv, statusOn};
            try (Cursor c = db.query(TABLE_NAME_MODULE, moduleProjectionSimple, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    result.add(c.getString(c.getColumnIndex(COLUMN_NAME_MODULE_NAME)));
                }
            }
        }
        return result;
    }
    // enable or disable speficic module for device
    // return nothing :)
    // lock read and write
    // arg status if true - on, false - off
    // method does not check whether such entry exist, maybe need carefully testing
    @Synchronized
    public void changeModuleStatus(String dv, String moduleName, boolean status) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_MODULE_NAME, moduleName);
            values.put(COLUMN_NAME_MODULE_DEVICE, dv);
            values.put(COLUMN_NAME_MODULE_STATUS, status ? statusOn : statusOff);
            db.insert(TABLE_NAME_MODULE, null, values);
        }


    }
    //*************************************************************************************************************************
    // getMissed calls from CallLog
    @Synchronized
    public List<MissedCall> getMissedCalls() {
        List<MissedCall> list = new ArrayList<>();
        String path = "content://call_log/calls";
        String[] projection = new String[]{CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE, CallLog.Calls._ID};
        String sortOrder = CallLog.Calls.DATE + " DESC";
        StringBuilder sb = new StringBuilder();
        sb.append(CallLog.Calls.TYPE).append("=?").append(" and ").append(CallLog.Calls.IS_READ).append("=?");
        try(Cursor cursor = context.getContentResolver().query(Uri.parse(path), projection, sb.toString(), new String[]{String.valueOf(CallLog.Calls.MISSED_TYPE), "0"}, sortOrder)) {
            if (cursor == null) {
                return list;
            }
            while (cursor.moveToNext()) {
                int count = cursor.getCount();
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                list.add(new MissedCall(number, cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)), count, cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)), callSmsUtils.getContactImage(context, number), cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID))));
            }
        }
        return list;
    }


    // return list of unread messages
    public List<Sms> getUnreadMessages() {
        List<Sms> result = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        final String[] projection = new String[]{"*"};
        try (Cursor cur = cr.query(Telephony.Sms.CONTENT_URI,  projection, Telephony.TextBasedSmsColumns.READ + " = ?", new String[]{"0"}, Telephony.Sms.Conversations.DEFAULT_SORT_ORDER)) {
            if(cur == null){
                return result;
            }
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex("_id"));
                String text = cur.getString(cur.getColumnIndexOrThrow("body"));
                String adress = cur.getString(cur.getColumnIndexOrThrow("address"));
                long date = cur.getLong(cur.getColumnIndexOrThrow("date"));
                String icon = callSmsUtils.getContactImage(context, adress);
                Sms sms = new Sms(  callSmsUtils.getName(adress,context), adress, text);
                sms.setType("sms");
                sms.setId(id);
                sms.setIcon(icon);
                sms.setDate(date);
                result.add(sms);
            }
        }
        return result;
    }

    public List<Sms> getUnreadMMs() {
        String read = "read = 0";
        List<Sms> result = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        try (Cursor curPdu = contentResolver.query(Uri.parse("content://mms"), null, read, null, null)) {
            if(curPdu == null){
                return result;
            }
            if (curPdu.moveToNext()) {
                String id = curPdu.getString(curPdu.getColumnIndex("_id"));
                String text = getMmsText(id);
                long date =   curPdu.getLong(2);
                String address = getAddressNumber(id);
                String icon = callSmsUtils.getContactImage(context, address);
                Sms sms = new Sms(  callSmsUtils.getName(address,context), address, text);
                sms.setId(id);
                sms.setType("mms");
                sms.setIcon(icon);
                sms.setDate(date);
                result.add(sms);
            }
        }
        return result;
    }

    private String getAddressNumber(String id) {
        String selectionAdd = "msg_id=" + id;
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = context.getContentResolver().query(uriAddress, null, selectionAdd, null, null);
        String name = null;
        if(cAdd == null){
            return "";
        }
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    name = number.replace("-", "");
                }
            } while (cAdd.moveToNext());
        }
        cAdd.close();
        return name;
    }

    private String getMmsText(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        StringBuilder sb = new StringBuilder();
        try (InputStream is = context.getContentResolver().openInputStream(partURI)) {
            if (is != null) {
                try (InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                     BufferedReader reader = new BufferedReader(isr)) {
                    String temp = reader.readLine();
                    while (temp != null) {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "error in getMmsText id: " + id, e);
        }
        return sb.toString();
    }

}
