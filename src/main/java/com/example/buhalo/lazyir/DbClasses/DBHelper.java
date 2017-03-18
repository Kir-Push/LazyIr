package com.example.buhalo.lazyir.DbClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.example.buhalo.lazyir.Devices.Command;
import java.util.ArrayList;
import java.util.List;



public class DBHelper extends SQLiteOpenHelper implements  DbCommands {

        private static final int DATABASE_VERSION = 12;
        private static final String DATABASE_NAME = "Buttons.db";
        private static DBHelper instance;

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
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_BUTTON);
            db.execSQL(SQL_CREATE_ENTRIES_LAYOUT);
            db.execSQL(SQL_CREATE_ENTRIES_COMMANDS);
            db.execSQL(SQL_CREATE_ENTRIES_BUTTON_COMMANDS);
            Command command = new Command("Komanda 1", "please decrease my volume, Thank you", null, "pc");
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_TEXT, command.getCommand_name());
            values.put(COLUMN_NAME_COMMAND_TYPE, command.getType());
            values.put(COLUMN_NAME_COMMAND, command.getCommand());
            long newRowId;
            newRowId = db.insert(
                    TABLE_NAME_COMMANDS,
                    null,
                    values);
            command = new Command("Samsung TV Power", "0000 006d 0022 0003 00a9 00a8 0015 003f 0015 003f 0015 003f 0015 " +
                    "0015 0015 0015 0015 0015 0015 0015 " +
                    "0015 0015 0015 003f 0015 003f 0015 003f 0015 0015 0015 0015 0015 0015 0015 0015" +
                    " 0015 0015 0015 0015 0015 003f 0015 0015 0015 " +
                    "0015 0015 0015 " +
                    "0015 0015 0015 0015 0015 0015 0015 0040 0015 0015 0015 003f 0015 003f 0015 003f " +
                    "0015 003f 0015 003f 0015 003f 0015 0702 00a9 00a8 0015 0015 0015 0e6e", null, "ir");
            values = new ContentValues();
            values.put(COLUMN_NAME_TEXT, command.getCommand_name());
            values.put(COLUMN_NAME_COMMAND_TYPE, command.getType());
            values.put(COLUMN_NAME_COMMAND, command.getCommand());
            newRowId = db.insert(
                    TABLE_NAME_COMMANDS,
                    null,
                    values);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES_BUTTON);
            db.execSQL(SQL_DELETE_ENTRIES_LAYOUT);
            db.execSQL(SQL_DELETE_ENTRIES_COMMANDS);
            db.execSQL(SQL_DELETE_ENTRIES_COMMANDS_BTN);
            onCreate(db);
        }


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
            Log.d("DB", "!!!!!!!!!!!!!!!!!!!!! BEFORE update BUTTON ID: " + button.getId());
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
                System.out.println("BUtton size? on update " + button.getLayoutParams().width + "  " + button.getLayoutParams().height);
                values.put(COLUMN_NAME_RES_ID,(String)button.getTag());
                long newRowId;
                String selelection = _ID + " LIKE ?";
                newRowId = db.update(
                        TABLE_NAME_BUTTON,
                        values,
                        selelection, new String[]{String.valueOf(button.getId())});
                //  button.setId((int)newRowId);
                Log.d("DB", "!!!!!!!!!!!!!!!!!!!!!update BUTTON ID: " + button.getId());
                int i = 0;
//                removeLayoutBtn(String.valueOf(button.getId()));
                String selectionD= COLUMN_NAME_ENTRY_ID + " LIKE ?";
                String[] selectionArgsD = {String.valueOf(button.getId())};
                db.delete(TABLE_NAME_LAYOUT, selectionD, selectionArgsD);
                for (int rule : params.getRules()) {
                    layout_values.put(COLUMN_NAME_ENTRY_ID, newRowId);
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

        public void saveCommand(Command command) {
            try (SQLiteDatabase db = getWritableDatabase()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_TEXT, command.getCommand_name());
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
                        COLUMN_NAME_COMMAND_TYPE,
                        COLUMN_NAME_COMMAND

                };
                String selection = null;
                String[] selectionArgs = null;
                if (command_name != null) {
                    selection = COLUMN_NAME_TEXT + " LIKE ?";
                    selectionArgs = new String[]{command_name};
                }
                try (Cursor c = db.query(TABLE_NAME_COMMANDS, projection, selection, selectionArgs, null, null, null)) {
                    while (c.moveToNext()) {
                        Command command = new Command(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), null, c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND_TYPE)));
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

        public List<Command> getBtnCommands(String id) {
            List<Command> commands = new ArrayList<Command>();
            try (SQLiteDatabase db = getReadableDatabase()) {
                String[] projection = {
                        COLUMN_NAME_ENTRY_ID,
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
                        Command command = new Command(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND)), String.valueOf(c.getInt(c.getColumnIndex(COLUMN_NAME_ENTRY_ID))), c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND_TYPE)));
                        commands.add(command);
                    }
                }
            }
            return commands;
        }


        public List<android.widget.Button> getButtons(String tab, Context cntx) {

            List<android.widget.Button> buttonList = new ArrayList<android.widget.Button>();
            SQLiteDatabase db = getReadableDatabase();
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
                    btn.setLayoutParams(new RelativeLayout.LayoutParams(c.getInt(c.getColumnIndex(COLUMN_NAME_WIDTH)),c.getInt(c.getColumnIndex(COLUMN_NAME_HEIGH))));
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
                };

            }
            db.close();
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

        public void removeCommandBtn(String id, String commandName) {
            try (SQLiteDatabase db = getWritableDatabase()) {
                String selelection = COLUMN_NAME_ENTRY_ID + " LIKE ? AND " + COLUMN_NAME_TEXT + " LIKE ?";
                String[] selectionArgs = {id, commandName};
                db.delete(TABLE_NAME_COMMANDS_BTN, selelection, selectionArgs);
            }
        }


        private void removeCommandBtnAll(String id) {
            try (SQLiteDatabase db = getWritableDatabase()) {
                String selelection = COLUMN_NAME_ENTRY_ID + " LIKE ?";
                String[] selectionArgs = {id};
                db.delete(TABLE_NAME_COMMANDS_BTN, selelection, selectionArgs);
            }
        }

        public void removeCommandsAll()
        {
            try (SQLiteDatabase db = getWritableDatabase()) {
                db.delete(TABLE_NAME_COMMANDS_BTN, null, null);
            }
        }

    }
