package com.example.buhalo.lazyir.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;
import android.widget.RelativeLayout;

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
    private static final int DATABASE_VERSION = 34;
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
    // Layout actions - button's update, create, delete and getting.
    public void saveButton(android.widget.Button button, String tab) {
        if (button == null) {
            return;
        }
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            ContentValues contentValues = new ContentValues();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
            values.put(COLUMN_NAME_TEXT, button.getText().toString());
            values.put(COLUMN_NAME_COORD_X, button.getX());
            values.put(COLUMN_NAME_COORD_Y, button.getY());
            values.put(COLUMN_NAME_TAB, tab);
            values.put(COLUMN_NAME_WIDTH, button.getLayoutParams().width);
            values.put(COLUMN_NAME_HEIGH, button.getLayoutParams().height);
            values.put(COLUMN_NAME_RES_ID, (String) button.getTag());
            long newRowId;
            newRowId = db.insert(TABLE_NAME_BUTTON, null, values);
            button.setId((int) newRowId);
            int i = 0;
            for (int rule : params.getRules()) {
                contentValues.put(COLUMN_NAME_ENTRY_ID, newRowId);
                contentValues.put(COLUMN_NAME_LAYOUT_PARAMS, i);
                contentValues.put(COLUMN_NAME_LAYOUT_VALUE, rule);
                newRowId = db.insert(TABLE_NAME_LAYOUT, null, contentValues);
                i++;
            }
        }
    }

    public void updateButton(android.widget.Button button, String tab) {
        if (button == null) {
            return;
        }
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            ContentValues contentValues = new ContentValues();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
            values.put(COLUMN_NAME_TEXT, button.getText().toString());
            values.put(COLUMN_NAME_COORD_X, button.getX());
            values.put(COLUMN_NAME_COORD_Y, button.getY());
            values.put(COLUMN_NAME_TAB, tab);
            values.put(COLUMN_NAME_WIDTH, button.getLayoutParams().width);
            values.put(COLUMN_NAME_HEIGH, button.getLayoutParams().height);
            values.put(COLUMN_NAME_RES_ID, (String) button.getTag());
            String selelection = _ID + LIKE;
            db.update(TABLE_NAME_BUTTON, values, selelection, new String[]{String.valueOf(button.getId())});
            int i = 0;
            String selectionD = COLUMN_NAME_ENTRY_ID + LIKE;
            String[] selectionArgsD = {String.valueOf(button.getId())};
            db.delete(TABLE_NAME_LAYOUT, selectionD, selectionArgsD);
            for (int rule : params.getRules()) {
                contentValues.put(COLUMN_NAME_ENTRY_ID, button.getId());
                contentValues.put(COLUMN_NAME_LAYOUT_PARAMS, i);
                contentValues.put(COLUMN_NAME_LAYOUT_VALUE, rule);
                db.insert(TABLE_NAME_LAYOUT, null, contentValues);
                i++;
            }
        }
    }

    public List<android.widget.Button> getButtons(String tab, Context cntx) {
        List<android.widget.Button> buttonList = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    _ID,
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COORD_X,
                    COLUMN_NAME_COORD_Y,
                    COLUMN_NAME_TAB,
                    COLUMN_NAME_WIDTH,
                    COLUMN_NAME_HEIGH,
                    COLUMN_NAME_RES_ID
            };
            String selectionBtn = COLUMN_NAME_TAB + LIKE;
            String[] selectionArgsBtn = {tab};
            try (Cursor c = db.query(TABLE_NAME_BUTTON, projection, selectionBtn, selectionArgsBtn, null, null, null)) {
                while (c.moveToNext()) {
                    android.widget.Button btn = new android.widget.Button(cntx);
                    btn.setId(c.getInt(c.getColumnIndex(_ID)));
                    btn.setText(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)));
                    btn.setY(c.getFloat(c.getColumnIndex(COLUMN_NAME_COORD_Y)));
                    btn.setX(c.getFloat(c.getColumnIndex(COLUMN_NAME_COORD_X)));
                    btn.setLayoutParams(new RelativeLayout.LayoutParams(c.getInt(c.getColumnIndex(COLUMN_NAME_WIDTH)), c.getInt(c.getColumnIndex(COLUMN_NAME_HEIGH))));
                    btn.setTag(c.getString(c.getColumnIndex(COLUMN_NAME_RES_ID)));
                    buttonList.add(btn);
                }
            }
            String[] projectionL = {
                    COLUMN_NAME_ENTRY_ID,
                    COLUMN_NAME_LAYOUT_PARAMS,
                    COLUMN_NAME_LAYOUT_VALUE
            };
            String selection = COLUMN_NAME_ENTRY_ID + LIKE;
            for (android.widget.Button button : buttonList) {
                String[] selectionArgs = {String.valueOf(button.getId())};
                try (Cursor c = db.query(TABLE_NAME_LAYOUT, projectionL, selection, selectionArgs, null, null, null)) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) button.getLayoutParams();
                    while (c.moveToNext()) {
                        layoutParams.addRule(c.getInt(c.getColumnIndex(COLUMN_NAME_LAYOUT_PARAMS)), c.getInt(c.getColumnIndex(COLUMN_NAME_LAYOUT_VALUE)));
                    }
                    button.setLayoutParams(layoutParams);
                }
            }
        }
        return buttonList;
    }

    public void removeButton(String id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selection = _ID + LIKE;
            String selection2 = COLUMN_NAME_ENTRY_ID + LIKE;
            String[] selectionArgs = {id};
            db.delete(TABLE_NAME_BUTTON, selection, selectionArgs);
            db.delete(TABLE_NAME_LAYOUT, selection2, selectionArgs);
        }
        removeCommandBtnAll(id);
    }

    //************************************************************************************************************
    // Global commands CRUD action's and
    public void saveCommand(Command command) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_TEXT, command.getCommandName());
            values.put(COLUMN_NAME_COMAMND_VENDOR, command.getProducer());
            values.put(COLUMT_NAME_COMMAND_DEVICE, command.getDevice());
            values.put(COLUMN_NAME_COMMAND_TYPE, command.getType());
            values.put(COLUMN_NAME_COMMAND, command.getCmd());
            db.insert(TABLE_NAME_COMMANDS, null, values);
        }
    }

    public List<Command> getCommand(String commandName) {
        List<Command> commands = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMAMND_VENDOR,
                    COLUMT_NAME_COMMAND_DEVICE,
                    COLUMN_NAME_COMMAND_TYPE,
                    COLUMN_NAME_COMMAND

            };
            String selection = null;
            String[] selectionArgs = null;
            if (commandName != null) {
                selection = COLUMN_NAME_TEXT + LIKE_AND + COLUMN_NAME_COMMAND_TYPE + LIKE;
                selectionArgs = new String[]{commandName, "pc"};
            }
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    Command command = new Command(c.getString(c.getColumnIndex(COLUMN_NAME_COMAMND_VENDOR)), c.getString(c.getColumnIndex(COLUMT_NAME_COMMAND_DEVICE)), c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), null, c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND_TYPE)));
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    public int updateCommand(Command command){
        try(SQLiteDatabase db = getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_COMAMND_VENDOR,command.getProducer());
            values.put(COLUMN_NAME_MODULE_DEVICE,command.getDevice());
            values.put(COLUMN_NAME_COMMAND,command.getCmd());
            String selection = COLUMN_NAME_TEXT + LIKE_AND + COLUMN_NAME_COMMAND_TYPE + LIKE;
            String[] selectionArgs = new String[]{command.getCommandName(), "pc"};
            return db.update(TABLE_NAME_COMMANDS, values, selection, selectionArgs);
        }
    }

    public List<Command> getCommandFull() {
        List<Command> commands = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMAMND_VENDOR,
                    COLUMT_NAME_COMMAND_DEVICE,
                    COLUMN_NAME_COMMAND_TYPE,
                    COLUMN_NAME_COMMAND

            };
            String selection;
            String[] selectionArgs;
            selection = COLUMN_NAME_COMMAND_TYPE + LIKE;
            selectionArgs = new String[]{"pc"};
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    Command command = new Command(c.getString(c.getColumnIndex(COLUMN_NAME_COMAMND_VENDOR)), c.getString(c.getColumnIndex(COLUMT_NAME_COMMAND_DEVICE)), c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), null, c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND_TYPE)));
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    public List<String> getCommandsPc() {
        List<String> commands = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMAMND_VENDOR,
                    COLUMT_NAME_COMMAND_DEVICE,
                    COLUMN_NAME_COMMAND_TYPE,
                    COLUMN_NAME_COMMAND

            };
            String selection = COLUMN_NAME_COMMAND_TYPE + LIKE;
            String[] selectionArgs = new String[]{"pc"};
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    commands.add(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)));
                }
            }
        }
        return commands;
    }
    // delete command from DB
    public void deleteCommand(Command command) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selection = COLUMN_NAME_COMMAND_TYPE + LIKE_AND + COLUMN_NAME_TEXT + LIKE;
            String[] selectionArgs = {"pc", command.getCmd()};
            db.delete(TABLE_NAME_COMMANDS, selection, selectionArgs);
        }
    }
    //************************************************************************************************************
    // Button's commands action's - delete, add, remove and so on
    // remove specific command from specific button
    public void removeCommandBtn(String id, String commandName) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selelection = COLUMN_NAME_ENTRY_ID + LIKE_AND + COLUMN_NAME_TEXT + LIKE;
            String[] split = commandName.split("_");
            if (split.length < 3) {
                String[] selectionArgs = {id, commandName};
                db.delete(TABLE_NAME_COMMANDS_BTN, selelection, selectionArgs);
            } else {
                selelection = COLUMN_NAME_ENTRY_ID + LIKE_AND + COLUMN_NAME_TEXT + LIKE_AND + COLUMT_NAME_COMMAND_DEVICE + LIKE_AND + COLUMN_NAME_COMAMND_VENDOR + LIKE;
                String[] selectionArgs = {id, split[2], split[1], split[0]};
                db.delete(TABLE_NAME_COMMANDS_BTN, selelection, selectionArgs);
            }
        }
    }

    public int updateBtnCommand(Command command){
        try(SQLiteDatabase db = getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_COMAMND_VENDOR,command.getProducer());
            values.put(COLUMN_NAME_MODULE_DEVICE,command.getDevice());
            values.put(COLUMN_NAME_COMMAND,command.getCmd());
            values.put(COLUMN_NAME_COMMAND_TYPE, command.getType());
            String selection = COLUMN_NAME_ENTRY_ID + LIKE_AND + COLUMN_NAME_TEXT + LIKE;
            String[] selectionArgs = new String[]{command.getOwnerId(), command.getCommandName()};
            return db.update(TABLE_NAME_COMMANDS_BTN, values, selection, selectionArgs);
        }
    }

    // remove all button commands! (for specific button)
    private void removeCommandBtnAll(String id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selelection = COLUMN_NAME_ENTRY_ID + LIKE;
            String[] selectionArgs = {id};
            db.delete(TABLE_NAME_COMMANDS_BTN, selelection, selectionArgs);
        }
    }
    public List<Command> getBtnCommands(String id) {
        List<Command> commands = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_ENTRY_ID,
                    COLUMN_NAME_COMAMND_VENDOR,
                    COLUMT_NAME_COMMAND_DEVICE,
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMMAND_TYPE,
                    COLUMN_NAME_COMMAND

            };
            String selection;
            String[] selectionArgs;
            selection = COLUMN_NAME_ENTRY_ID + LIKE;
            selectionArgs = new String[]{id};
            try (Cursor c = db.query(TABLE_NAME_COMMANDS_BTN, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    String vendor = c.getString(c.getColumnIndex(COLUMN_NAME_COMAMND_VENDOR));
                    String device = c.getString(c.getColumnIndex(COLUMT_NAME_COMMAND_DEVICE));
                    String commandName = "";
                    if ((vendor != null && !vendor.equals("")) || (device != null && !device.equals(""))) {
                        commandName = vendor + "_" + device + "_";
                    }
                    commandName = commandName + c.getString(c.getColumnIndex(COLUMN_NAME_TEXT));
                    Command command = new Command(vendor, device, commandName, c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), String.valueOf(c.getInt(c.getColumnIndex(COLUMN_NAME_ENTRY_ID))), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND_TYPE)));
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    public void saveBtnCommand(String commandName, String id) {
        if (commandName == null) {
            return;
        }
        List<Command> commands = getCommand(commandName);
        try (SQLiteDatabase db = getWritableDatabase()) {
            for (Command command : commands) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_TEXT, command.getCommandName());
                values.put(COLUMN_NAME_COMAMND_VENDOR, command.getProducer());
                values.put(COLUMT_NAME_COMMAND_DEVICE, command.getDevice());
                values.put(COLUMN_NAME_ENTRY_ID, Integer.parseInt(id));
                values.put(COLUMN_NAME_COMMAND_TYPE, command.getType());
                values.put(COLUMN_NAME_COMMAND, command.getCmd());
                db.insert(TABLE_NAME_COMMANDS_BTN, null, values);
            }
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
            enabledModules = fillStandart(dv);
        }
        return enabledModules;
    }
    // populate module table for default enabled modules settings
    // it's called when device connect for the fisrt time,and no setting's for device in db
    // return list of enabled modules names by this method - actually all modules
    // lock for write and read
    @Synchronized
    private List<String> fillStandart(String dv) {
        List<String> result = new ArrayList<>();
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
        return result;
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
            String selection = COLUMN_NAME_MODULE_DEVICE + LIKE_AND + COLUMN_NAME_MODULE_NAME + LIKE;
            String[] args = new String[]{dv, moduleName};
            db.update(TABLE_NAME_MODULE, values, selection, args);
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
