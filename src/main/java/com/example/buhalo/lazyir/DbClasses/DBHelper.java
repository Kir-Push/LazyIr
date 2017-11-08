package com.example.buhalo.lazyir.DbClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.RelativeLayout;
import com.example.buhalo.lazyir.Devices.Command;
import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.modules.ModuleFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class DBHelper extends SQLiteOpenHelper implements  DbCommands {

        private static final int DATABASE_VERSION = 26;
        private static String DB_PATH = "";
        private static final String DATABASE_NAME = "Buttons.db";
        private static DBHelper instance;
        private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public static synchronized DBHelper getInstance(Context context)
        {
            if(instance == null)
            {
                instance = new DBHelper(context);
            }
            return instance;
        }

        private DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
         //   DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
            // path where databases has to be
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
            createDataBase(context);
        }

    //************************************************************************************************************
    // Utility method's

    // checking whether database file - lie in folder.
    private boolean checkDataBase()
    {
        File dbFile = new File(DB_PATH + DATABASE_NAME);
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }

    // copy database from file in project(default, init DB file), which will be in asset's folder, to
    // filer specified in DATABASE_NAME const. Which are App db file
    private void copyDataBase(Context context) throws IOException
    {
        InputStream mInput = context.getApplicationContext().getAssets().open(DATABASE_NAME);
        String outFileName = DB_PATH + DATABASE_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }


    private void createDataBase(Context context)
    {
        //If the database does not exist, copy it from the assets.

        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist)
        {
            this.getReadableDatabase();
            this.close();
            try
            {
                //Copy the database from assests
                copyDataBase(context);
                Log.e("DB", "createDatabase database created");
            }
            catch (IOException mIOException)
            {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    //************************************************************************************************************

        @Override
        public void onCreate(SQLiteDatabase db) {

        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }


    //************************************************************************************************************
    // Pairing action's - getCode and CRUD's

        public void savePairedDevice(String dvId,String pairCode)
        {
            try(SQLiteDatabase db = getWritableDatabase())
            {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_DVID,dvId);
                values.put(COLUMN_NAME_PAIRCODE,pairCode);
                long newRowId = db.insert(TABLE_NAME_PAIRED_DEVICES,null,values);
            }
        }

        public void updatePaired(String dvId,String pairCode)
        {
            try(SQLiteDatabase db = getWritableDatabase())
            {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_DVID,dvId);
                values.put(COLUMN_NAME_PAIRCODE,pairCode);
                String selection = COLUMN_NAME_DVID + " LIKE ?";
                String[] args = new String[]{dvId};
                db.update(TABLE_NAME_PAIRED_DEVICES,values,selection,args);
            }
        }

        public void deletePaired(String dvId)
        {
            try(SQLiteDatabase db = getWritableDatabase())
            {
                String selection = COLUMN_NAME_DVID + " LIKE ?";
                String[] args = new String[]{dvId};
                db.delete(TABLE_NAME_PAIRED_DEVICES,selection,args);
            }
        }

        public List<String> getPairedCode(String dvId)
        {
            List<String> result = new ArrayList<>();
            try (SQLiteDatabase db = getReadableDatabase()) {
                String[] projection = {
                        COLUMN_NAME_PAIRCODE
                };
                String selection = COLUMN_NAME_DVID + " LIKE ?";
                String[] args = new String[]{dvId};
                try (Cursor c = db.query(TABLE_NAME_PAIRED_DEVICES,projection,selection,args,null,null,null))
                {
                    while (c.moveToNext())
                    {
                        result.add(c.getString(c.getColumnIndex(COLUMN_NAME_PAIRCODE)));
                    }
                }

            }
            return result;
        }


    //************************************************************************************************************


    //************************************************************************************************************
    // Layout actions - button's update, create, delete and getting.

    public void saveButton(android.widget.Button button, String tab) {
            if (button == null) {
                return;
            }
            try (SQLiteDatabase db = getWritableDatabase()) {
                ContentValues values = new ContentValues();
                ContentValues layout_values = new ContentValues();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
                values.put(COLUMN_NAME_TEXT, button.getText().toString());
                values.put(COLUMN_NAME_COORD_X, button.getX());
                values.put(COLUMN_NAME_COORD_Y, button.getY());
                values.put(COLUMN_NAME_TAB, tab);
                values.put(COLUMN_NAME_WIDTH, button.getLayoutParams().width);
                values.put(COLUMN_NAME_HEIGH, button.getLayoutParams().height);
                System.out.println("BUtton size? " + button.getLayoutParams().width + "  " + button.getLayoutParams().height);
                values.put(COLUMN_NAME_RES_ID,(String)button.getTag());
                long newRowId;
                newRowId = db.insert(
                        TABLE_NAME_BUTTON,
                        null,
                        values);
                button.setId((int) newRowId);
                Log.d("DB", "!!!!!!!!!!!!!!!!!!!!!save BUTTON ID: " + button.getId());
                int i = 0;
                for (int rule : params.getRules()) {
                    layout_values.put(COLUMN_NAME_ENTRY_ID, newRowId);
                    layout_values.put(COLUMN_NAME_LAYOUT_PARAMS, i);
                    layout_values.put(COLUMN_NAME_LAYOUT_VALUE, rule);
                    newRowId = db.insert(
                            TABLE_NAME_LAYOUT,
                            null,
                            layout_values);
                    i++;
                }
            }

        }

        public void updateButton(android.widget.Button button, String tab) {
            if (button == null) {
                return;
            }
            Log.d("DB", "BEFORE update BUTTON ID: " + button.getId());
            try (SQLiteDatabase db = getWritableDatabase()) {
                ContentValues values = new ContentValues();
                ContentValues layout_values = new ContentValues();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
                values.put(COLUMN_NAME_TEXT, button.getText().toString());
                values.put(COLUMN_NAME_COORD_X, button.getX());
                values.put(COLUMN_NAME_COORD_Y, button.getY());
                values.put(COLUMN_NAME_TAB, tab);
                values.put(COLUMN_NAME_WIDTH, button.getLayoutParams().width);
                values.put(COLUMN_NAME_HEIGH, button.getLayoutParams().height);
                values.put(COLUMN_NAME_RES_ID,(String)button.getTag());
                long newRowId;
                String selelection = _ID + " LIKE ?";
                newRowId = db.update(
                        TABLE_NAME_BUTTON,
                        values,
                        selelection, new String[]{String.valueOf(button.getId())});
                Log.d("DB", "update BUTTON ID: " + button.getId());
                int i = 0;
                String selectionD= COLUMN_NAME_ENTRY_ID + " LIKE ?";
                String[] selectionArgsD = {String.valueOf(button.getId())};
                db.delete(TABLE_NAME_LAYOUT, selectionD, selectionArgsD);
                for (int rule : params.getRules()) {
                    layout_values.put(COLUMN_NAME_ENTRY_ID, button.getId());
                    layout_values.put(COLUMN_NAME_LAYOUT_PARAMS, i);
                    layout_values.put(COLUMN_NAME_LAYOUT_VALUE, rule);
                    db.insert(
                            TABLE_NAME_LAYOUT,
                            null,
                            layout_values);
                    i++;
                }
            }
        }


        public  List<android.widget.Button> getButtons(String tab, Context cntx) {

            List<android.widget.Button> buttonList = new ArrayList<android.widget.Button>();
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

                String selection_btn = COLUMN_NAME_TAB + " LIKE ?";
                String[] selectionArgs_btn = {tab};
                try (Cursor c = db.query(TABLE_NAME_BUTTON, projection, selection_btn, selectionArgs_btn, null, null, null)) {
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

                String[] projection_l = {
                        COLUMN_NAME_ENTRY_ID,
                        COLUMN_NAME_LAYOUT_PARAMS,
                        COLUMN_NAME_LAYOUT_VALUE
                };
                String selection = COLUMN_NAME_ENTRY_ID + " LIKE ?";

                for (android.widget.Button button : buttonList) {
                    System.out.println(button.getId());

                    String[] selectionArgs = {String.valueOf(button.getId())};

                    try (Cursor c = db.query(TABLE_NAME_LAYOUT, projection_l, selection, selectionArgs, null, null, null)) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) button.getLayoutParams();
                        while (c.moveToNext()) {
                            layoutParams.addRule(c.getInt(c.getColumnIndex(COLUMN_NAME_LAYOUT_PARAMS)), c.getInt(c.getColumnIndex(COLUMN_NAME_LAYOUT_VALUE)));
                        }
                        button.setLayoutParams(layoutParams);
                    }


                }
                db.close();
            }
            return buttonList;
        }

        public void removeButton(String id) {
            Log.d("DB", "!!!!!!!!!!!!!!!!!!!!!remove BUTTON ID: " + id);
            try (SQLiteDatabase db = getWritableDatabase()) {
                String selection = _ID + " LIKE ?";
                String selection2 = COLUMN_NAME_ENTRY_ID + " LIKE ?";
                String[] selectionArgs = {id};
                db.delete(TABLE_NAME_BUTTON, selection, selectionArgs);
                db.delete(TABLE_NAME_LAYOUT, selection2, selectionArgs);
            }
            removeCommandBtnAll(id);

        }

        public void removeLayoutBtn(String id)
        {
            Log.d("DB", "!!!!!!!!!!!!!!!!!!!!!remove Layout BUTTON ID: " + id);
            try (SQLiteDatabase db = getWritableDatabase()) {
                String selection2 = COLUMN_NAME_ENTRY_ID + " LIKE ?";
                String[] selectionArgs = {id};
                db.delete(TABLE_NAME_LAYOUT, selection2, selectionArgs);
            }
        }
    //************************************************************************************************************



    //************************************************************************************************************
    // Global commands CRUD action's and

    public void saveCommand(Command command) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_TEXT, command.getCommand_name());
            values.put(COLUMN_NAME_COMAMND_VENDOR,command.getProducer());
            values.put(COLUMT_NAME_COMMAND_DEVICE,command.getDevice());
            values.put(COLUMN_NAME_COMMAND_TYPE, command.getType());
            values.put(COLUMN_NAME_COMMAND, command.getCommand());
            long newRowId;
            newRowId = db.insert(
                    TABLE_NAME_COMMANDS,
                    null,
                    values);
        }
    }

    public List<Command> getCommand(String command_name) {
        List<Command> commands = new ArrayList<Command>();
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
            if (command_name != null) {
                selection = COLUMN_NAME_TEXT + " LIKE ? AND " + COLUMN_NAME_COMMAND_TYPE + " LIKE ?";
                selectionArgs = new String[]{command_name,"pc"};
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

    public List<Command> getCommandFull() {
        List<Command> commands = new ArrayList<Command>();
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
            selection =  COLUMN_NAME_COMMAND_TYPE + " LIKE ?";
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

    public List<String> getCommandsPc()
    {
        List<String> commands = new ArrayList<String>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMAMND_VENDOR,
                    COLUMT_NAME_COMMAND_DEVICE,
                    COLUMN_NAME_COMMAND_TYPE,
                    COLUMN_NAME_COMMAND

            };
            String selection = COLUMN_NAME_COMMAND_TYPE + " LIKE ?";
            String[] selectionArgs =new String[]{"pc"};
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    commands.add( c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)));
                }
            }
        }
        return commands;
    }



    public void removeCommandsAll()
        {
            try (SQLiteDatabase db = getWritableDatabase()) {
                db.delete(TABLE_NAME_COMMANDS_BTN, null, null);
            }
        }

        public void removeCommandsPcAll()
        {
            try (SQLiteDatabase db = getWritableDatabase()) {
                String selection = COLUMN_NAME_COMMAND_TYPE + " LIKE ?";
                String[] selectionArgs = {"pc"};
                db.delete(TABLE_NAME_COMMANDS, selection, selectionArgs);
            }
        }



    public List<String> getProducerList() {
        Set<String> set = new HashSet<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_COMAMND_VENDOR,

            };
            String selection = COLUMN_NAME_COMMAND_TYPE + " LIKE ?";
            String[] selectionArgs = new String[]{"ir"};
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    String string = c.getString(c.getColumnIndex(COLUMN_NAME_COMAMND_VENDOR));
                    if(string != null)
                    set.add(string);
                }
            }
        }
        return new ArrayList<>(set);
    }



    public List<String> getTypeByProducer(String entry) {
        Set<String> set = new HashSet<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMT_NAME_COMMAND_DEVICE,

            };
            String selection = null;
            String[] selectionArgs = null;
            if (entry != null) {
                selection = COLUMN_NAME_COMAMND_VENDOR + " LIKE ?";
                selectionArgs = new String[]{entry};
            }
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    String string = c.getString(c.getColumnIndex(COLUMT_NAME_COMMAND_DEVICE));
                    if(string != null)
                        set.add(string);
                }
            }
        }
        return new ArrayList<>(set);
    }



    public List<String> getCodeNamesByType(String deviceType,String producer) {
        List<String> arrayList = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMAMND_VENDOR,
                    COLUMT_NAME_COMMAND_DEVICE

            };
            String selection = null;
            String[] selectionArgs = null;
            if (deviceType != null && producer != null) {
                selection = COLUMN_NAME_COMAMND_VENDOR + " LIKE ? AND " + COLUMT_NAME_COMMAND_DEVICE + " LIKE ?";
                selectionArgs = new String[]{producer,deviceType};
            }
            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    String string = c.getString(c.getColumnIndex(COLUMN_NAME_TEXT));
                    if(string != null)
                    arrayList.add(string);
                }
            }
        }
        return arrayList;
    }





    // get Ir (infra remote) command from db, possibly in future rewrite.
    private List<Command> getCommandIr(String thirdLevelAdapterItem, String secondLevel, String item) {
        List<Command> commands = new ArrayList<Command>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_COMAMND_VENDOR,
                    COLUMT_NAME_COMMAND_DEVICE,
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMMAND_TYPE,
                    COLUMN_NAME_COMMAND

            };
            String selection = null;
            String[] selectionArgs = null;
            selection = COLUMN_NAME_COMAMND_VENDOR + " LIKE ? AND " + COLUMT_NAME_COMMAND_DEVICE + " LIKE ? AND " + COLUMN_NAME_TEXT + " LIKE ?";
            selectionArgs = new String[]{secondLevel,item,thirdLevelAdapterItem};

            try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    Command command = new Command(c.getString(c.getColumnIndex(COLUMN_NAME_COMAMND_VENDOR)), c.getString(c.getColumnIndex(COLUMT_NAME_COMMAND_DEVICE)), c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), null, c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND_TYPE)));
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    // delete command from DB
    public void deleteCommand(Command command) {
        try(SQLiteDatabase db = getWritableDatabase())
        {
            String selection = COLUMN_NAME_COMMAND_TYPE + " LIKE ? AND " + COLUMN_NAME_TEXT + " LIKE ?";
            String[] selectionArgs = {"pc",command.getCommand()};
            db.delete(TABLE_NAME_COMMANDS, selection, selectionArgs);
        }
    }

    //************************************************************************************************************




    //************************************************************************************************************
    // Button's commands action's - delete, add, remove and so on

    // remove specific command from specific button
    public void removeCommandBtn(String id, String commandName) { //todo test here
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selelection = COLUMN_NAME_ENTRY_ID + " LIKE ? AND " + COLUMN_NAME_TEXT + " LIKE ?";
            String[] split = commandName.split("_");
            if(split.length < 3)
            {
                String[] selectionArgs = {id, commandName};
                db.delete(TABLE_NAME_COMMANDS_BTN, selelection, selectionArgs);
            }else
            {
                selelection = COLUMN_NAME_ENTRY_ID + " LIKE ? AND " + COLUMN_NAME_TEXT + " LIKE ? AND " + COLUMT_NAME_COMMAND_DEVICE + " LIKE ? AND " + COLUMN_NAME_COMAMND_VENDOR + " LIKE ?";
                String[] selectionArgs = {id, split[2],split[1],split[0]};
                db.delete(TABLE_NAME_COMMANDS_BTN, selelection, selectionArgs);
            }

        }
    }

    // save command associated with button.
    public void saveBtnCommandTemp(String thirdLevelAdapterItem, String secondLevel, String item,int btnId) {
        Log.d("IrActivity", item);
        List<Command> cmd  = getCommandIr(thirdLevelAdapterItem,secondLevel,item);
        try (SQLiteDatabase db = getWritableDatabase()) {
            for (Command command : cmd) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_TEXT, command.getCommand_name());
                values.put(COLUMN_NAME_COMAMND_VENDOR,command.getProducer());
                values.put(COLUMT_NAME_COMMAND_DEVICE,command.getDevice());
                values.put(COLUMN_NAME_ENTRY_ID, btnId);
                values.put(COLUMN_NAME_COMMAND_TYPE, command.getType());
                values.put(COLUMN_NAME_COMMAND, command.getCommand());
                long newRowId;
                newRowId = db.insert(
                        TABLE_NAME_COMMANDS_BTN,
                        null,
                        values);
            }
        }
    }
    // remove all button commands! (for specific button)
    private void removeCommandBtnAll(String id) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            String selelection = COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {id};
            db.delete(TABLE_NAME_COMMANDS_BTN, selelection, selectionArgs);
        }
    }


    public List<Command> getBtnCommands(String id) {
        List<Command> commands = new ArrayList<Command>();
        try (SQLiteDatabase db = getReadableDatabase()) {
            String[] projection = {
                    COLUMN_NAME_ENTRY_ID,
                    COLUMN_NAME_COMAMND_VENDOR,
                    COLUMT_NAME_COMMAND_DEVICE,
                    COLUMN_NAME_TEXT,
                    COLUMN_NAME_COMMAND_TYPE,
                    COLUMN_NAME_COMMAND

            };
            String selection = null;
            String[] selectionArgs = null;
            selection = COLUMN_NAME_ENTRY_ID + " LIKE ?";
            selectionArgs = new String[]{id};

            try (Cursor c = db.query(TABLE_NAME_COMMANDS_BTN, projection, selection, selectionArgs, null, null, null)) {
                while (c.moveToNext()) {
                    String vendor = c.getString(c.getColumnIndex(COLUMN_NAME_COMAMND_VENDOR));
                    String device = c.getString(c.getColumnIndex(COLUMT_NAME_COMMAND_DEVICE));
                    String command_name = "";
                    if((vendor != null && !vendor.equals("")) || (device != null && !device.equals("")) )
                    {
                        command_name = vendor + "_" + device + "_";
                    }
                    command_name = command_name + c.getString(c.getColumnIndex(COLUMN_NAME_TEXT));
                    Command command = new Command(vendor, device,command_name, c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), String.valueOf(c.getInt(c.getColumnIndex(COLUMN_NAME_ENTRY_ID))), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND_TYPE)));
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    public void saveBtnCommand(String command_name, String id) {
        if (command_name == null) {
            return;
        }
        List<Command> commands = getCommand(command_name);
        try (SQLiteDatabase db = getWritableDatabase()) {
            for (Command command : commands) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_TEXT, command.getCommand_name());
                values.put(COLUMN_NAME_COMAMND_VENDOR,command.getProducer());
                values.put(COLUMT_NAME_COMMAND_DEVICE,command.getDevice());
                values.put(COLUMN_NAME_ENTRY_ID, Integer.parseInt(id));
                values.put(COLUMN_NAME_COMMAND_TYPE, command.getType());
                values.put(COLUMN_NAME_COMMAND, command.getCommand());
                long newRowId;
                newRowId = db.insert(
                        TABLE_NAME_COMMANDS_BTN,
                        null,
                        values);
            }
        }
    }

    //************************************************************************************************************





    //************************************************************************************************************
    // Modules Info methods - get enabled, filling defaults, enabling and disabling and so on.


    // common variables for modules methods
    private String[] moduleProjectionSimple = {
            COLUMN_NAME_MODULE_NAME,
            COLUMN_NAME_MODULE_DEVICE,
            COLUMN_NAME_MODULE_STATUS };

    private String statusOn  = "ON";
    private String statusOff = "OFF";



    // first try getting enabledModulesBy device, if size 0, check if device non exist in db(first time connect, if true then fill by default list of enabled modules(all).
    // return enabledModules in each cases
    public List<String> checkAndSetDefaultIfNoInfo(Device dv) {
        List<String> enabledModules = getEnabledModules(dv);
        if(enabledModules.size() == 0 && checkIfFirstTimeDevice(dv))
            enabledModules =  fillStandart(dv);
        return enabledModules;
    }

    // populate module table for default enabled modules settings
    // it's called when device connect for the fisrt time,and no setting's for device in db
    // return list of enabled modules names by this method - actually all modules
    // lock for write and read
    private List<String> fillStandart(Device dv) {
            lock.writeLock().lock();
            lock.readLock().lock();
            List<String> result = new ArrayList<>();
            try(SQLiteDatabase db = getWritableDatabase()) {
                for (Class moduleClass : ModuleFactory.getRegisteredModules()) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_NAME_MODULE_NAME,moduleClass.getSimpleName());
                    values.put(COLUMN_NAME_MODULE_DEVICE,dv.getId());
                    values.put(COLUMN_NAME_MODULE_STATUS,statusOn);
                    long  newRowId = db.insert(
                            TABLE_NAME_MODULE,
                            null,
                            values);
                }

            }finally {
                lock.readLock().unlock();
                lock.writeLock().unlock();
            }
        return result;
    }


    // checking if device modules info exist in db.
    // simply checking module table for device id.
    // lock for write but not for read.
    // return true if non exist, otherwise false.
    private boolean checkIfFirstTimeDevice(Device dv) {
        lock.writeLock().lock();
        try(SQLiteDatabase db = getReadableDatabase()){
            String[] projection = {COLUMN_NAME_MODULE_DEVICE};
            String selection = COLUMN_NAME_MODULE_DEVICE + " LIKE ?";
            String[] selectionArgs = {dv.getId()};

            try(Cursor c = db.query(TABLE_NAME_MODULE,projection,selection,selectionArgs,null,null,null)) {
                return c.getCount() <= 0;
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    // get Enabled Modules for specific device,
    // lock for write but not for read.
    // return list of modules names
    public  List<String> getEnabledModules(Device dv){
        lock.writeLock().lock();
        List<String> result = new ArrayList<>();
        try(SQLiteDatabase db = getReadableDatabase()){

            String selection = COLUMN_NAME_MODULE_DEVICE + " LIKE ? AND " + COLUMN_NAME_MODULE_STATUS + " LIKE ?";
            String[] selectionArgs = {dv.getId(),statusOn};

            try(Cursor c = db.query(TABLE_NAME_MODULE,moduleProjectionSimple,selection,selectionArgs,null,null,null)) {
                while (c.moveToNext()){
                    result.add(c.getString(c.getColumnIndex(COLUMN_NAME_MODULE_NAME)));
                }
            }
        }finally {
            lock.writeLock().unlock();
        }
        return result;
    }

    // enable or disable speficic module for device
    // return nothing :)
    // lock read and write
    // arg status if true - on, false - off
    // method does not check whether such entry exist, maybe need carefully testing
    public void changeModuleStatus(Device dv, String moduleName,boolean status) {
        lock.writeLock().lock();
        lock.readLock().lock();
        try(SQLiteDatabase db = getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_MODULE_NAME,moduleName);
            values.put(COLUMN_NAME_MODULE_DEVICE,dv.getId());
            values.put(COLUMN_NAME_MODULE_STATUS,status ? statusOn : statusOff);
            String selection = COLUMN_NAME_MODULE_DEVICE + " LIKE ? AND " + COLUMN_NAME_MODULE_NAME + " LIKE ?";
            String[] args = new String[]{dv.getId(),moduleName};
            db.update(TABLE_NAME_MODULE,values,selection,args);
        }finally {
            lock.readLock().unlock();
            lock.writeLock().unlock();
        }
    }


    //*************************************************************************************************************************
}
