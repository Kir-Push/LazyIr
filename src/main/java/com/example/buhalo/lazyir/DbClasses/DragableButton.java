package com.example.buhalo.lazyir.DbClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.buhalo.lazyir.Devices.Command;

import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.COLUMN_NAME_COMMAND;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.COLUMN_NAME_ENTRY_ID;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.COLUMN_NAME_TEXT;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.COLUMN_NAME_COMMAND_TYPE;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_CREATE_ENTRIES_BUTTON;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_CREATE_ENTRIES_BUTTON_COMMANDS;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_CREATE_ENTRIES_COMMANDS;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_CREATE_ENTRIES_LAYOUT;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_DELETE_ENTRIES_BUTTON;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_DELETE_ENTRIES_COMMANDS;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_DELETE_ENTRIES_COMMANDS_BTN;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_DELETE_ENTRIES_LAYOUT;

/**
 * Created by buhalo on 05.02.17.
 */

public  class DragableButton {

    public DragableButton(){}

    public static abstract class Button implements BaseColumns
    {
        public static final String TABLE_NAME_BUTTON = "button";
        public static final String TABLE_NAME_LAYOUT = "layout";
        public static final String TABLE_NAME_COMMANDS = "commands";
        public static final String TABLE_NAME_COMMANDS_BTN = "commandsbtn";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_COMMAND = "command";
        public static final String COLUMN_NAME_COMMAND_TYPE = "type";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_COORD_X = "x";
        public static final String COLUMN_NAME_COORD_Y = "y";
        public static final String COLUMN_NAME_TAB = "tab";
        public static final String COLUMN_NAME_WIDTH = "width";
        public static final String COLUMN_NAME_HEIGH = "heigh";
        public static final String COLUMN_NAME_LAYOUT_PARAMS = "layoutpar";
        public static final String COLUMN_NAME_LAYOUT_VALUE = "value";
        public static final String TEXT_TYPE = " TEXT";
        public static final String COMMA_SEP = ",";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String SQL_CREATE_ENTRIES_BUTTON =
                "CREATE TABLE " + Button.TABLE_NAME_BUTTON + " (" +
                        Button._ID + " INTEGER PRIMARY KEY," +
                  //      Button.COLUMN_NAME_ENTRY_ID + INTEGER_TYPE + COMMA_SEP +
                        Button.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_COORD_X + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_COORD_Y + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_TAB + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_WIDTH + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_HEIGH + INTEGER_TYPE +
                        ")";
        public static final String SQL_CREATE_ENTRIES_LAYOUT =
                "CREATE TABLE " + Button.TABLE_NAME_LAYOUT + " (" +
                        Button._ID + " INTEGER PRIMARY KEY," +
                        Button.COLUMN_NAME_ENTRY_ID + Button.INTEGER_TYPE + COMMA_SEP +
                        Button.COLUMN_NAME_LAYOUT_PARAMS + Button.INTEGER_TYPE + COMMA_SEP +
                        Button.COLUMN_NAME_LAYOUT_VALUE + Button.INTEGER_TYPE +
                        ")";

        public static final String SQL_CREATE_ENTRIES_COMMANDS = //TODO create unique name index !!!
                "CREATE TABLE " + Button.TABLE_NAME_COMMANDS + " (" +
                        Button._ID + " INTEGER PRIMARY KEY," +
                        Button.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                        Button.COLUMN_NAME_COMMAND_TYPE + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_COMMAND + TEXT_TYPE + ")";

        public static final String SQL_CREATE_ENTRIES_BUTTON_COMMANDS =
                "CREATE TABLE " + Button.TABLE_NAME_COMMANDS_BTN + " (" +
                        Button._ID + " INTEGER PRIMARY KEY," +
                        Button.COLUMN_NAME_ENTRY_ID + INTEGER_TYPE + COMMA_SEP +
                        Button.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                        Button.COLUMN_NAME_COMMAND_TYPE + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_COMMAND + TEXT_TYPE + ")";



        public static final String SQL_DELETE_ENTRIES_BUTTON =
                "DROP TABLE IF EXISTS " + Button.TABLE_NAME_BUTTON;

        public static final String SQL_DELETE_ENTRIES_LAYOUT =
                "DROP TABLE IF EXISTS " + Button.TABLE_NAME_LAYOUT;

        public static final String SQL_DELETE_ENTRIES_COMMANDS =
                "DROP TABLE IF EXISTS " + Button.TABLE_NAME_COMMANDS;

        public static final String SQL_DELETE_ENTRIES_COMMANDS_BTN =
                "DROP TABLE IF EXISTS " + Button.TABLE_NAME_COMMANDS_BTN;
    }

    public class DbHelper extends SQLiteOpenHelper { //TODO SET SINGLTETON!!!

        public static final int DATABASE_VERSION = 7;
        public static final String DATABASE_NAME = "Buttons.db";

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_BUTTON);
            db.execSQL(SQL_CREATE_ENTRIES_LAYOUT);
            db.execSQL(SQL_CREATE_ENTRIES_COMMANDS);
            db.execSQL(SQL_CREATE_ENTRIES_BUTTON_COMMANDS);
            Command command = new Command("Komanda 1", "11111", null, "pc");
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_TEXT,command.getCommand_name());
            values.put(COLUMN_NAME_COMMAND_TYPE,command.getType());
            values.put(COLUMN_NAME_COMMAND,command.getCommand());
            long newRowId;
            newRowId = db.insert(
                    Button.TABLE_NAME_COMMANDS,
                    null,
                    values);
            command = new Command("Komanda 2", "22222", null, "ir");
            values = new ContentValues();
            values.put(COLUMN_NAME_TEXT,command.getCommand_name());
            values.put(COLUMN_NAME_COMMAND_TYPE,command.getType());
            values.put(COLUMN_NAME_COMMAND,command.getCommand());
            newRowId = db.insert(
                    Button.TABLE_NAME_COMMANDS,
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


        public void saveButton(android.widget.Button button, String tab) { // TODO problem in id button !!!!
            if (button == null) {
                //TODO
            }
            try (SQLiteDatabase db = getWritableDatabase()) {
                ContentValues values = new ContentValues();
                ContentValues layout_values = new ContentValues();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
                values.put(COLUMN_NAME_TEXT, button.getText().toString());
                values.put(Button.COLUMN_NAME_COORD_X, button.getX());
                values.put(Button.COLUMN_NAME_COORD_Y, button.getY());
                values.put(Button.COLUMN_NAME_TAB, tab);
                values.put(Button.COLUMN_NAME_WIDTH, button.getWidth());
                values.put(Button.COLUMN_NAME_HEIGH, button.getHeight());
                long newRowId;
                newRowId = db.insert(
                        Button.TABLE_NAME_BUTTON,
                        null,
                        values);
                button.setId((int)newRowId);
                Log.d("DB","!!!!!!!!!!!!!!!!!!!!!save BUTTON ID: " + button.getId());
                int i = 0;
                for (int rule : params.getRules()) {
                    layout_values.put(Button.COLUMN_NAME_ENTRY_ID, newRowId);
                    layout_values.put(Button.COLUMN_NAME_LAYOUT_PARAMS, i);
                    layout_values.put(Button.COLUMN_NAME_LAYOUT_VALUE, rule);
                    newRowId = db.insert(
                            Button.TABLE_NAME_LAYOUT,
                            null,
                            layout_values);
                    i++;
                }
            }

        }

        public void updateButton(android.widget.Button button,String tab)
        {
            if (button == null) {
                //TODO
            }
            Log.d("DB","!!!!!!!!!!!!!!!!!!!!! BEFORE update BUTTON ID: " + button.getId());
            try (SQLiteDatabase db = getWritableDatabase()) {
                ContentValues values = new ContentValues();
                ContentValues layout_values = new ContentValues();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
                values.put(COLUMN_NAME_TEXT, button.getText().toString());
                values.put(Button.COLUMN_NAME_COORD_X, button.getX());
                values.put(Button.COLUMN_NAME_COORD_Y, button.getY());
                values.put(Button.COLUMN_NAME_TAB, tab);
                values.put(Button.COLUMN_NAME_WIDTH, button.getWidth());
                values.put(Button.COLUMN_NAME_HEIGH, button.getHeight());
                long newRowId;
                String selelection = Button._ID + " LIKE ?";
                newRowId = db.update(
                        Button.TABLE_NAME_BUTTON,
                        values,
                        selelection,   new String[] { String.valueOf(button.getId()) });
              //  button.setId((int)newRowId);
                Log.d("DB","!!!!!!!!!!!!!!!!!!!!!update BUTTON ID: " + button.getId());
                int i = 0;
                for (int rule : params.getRules()) {
                    layout_values.put(Button.COLUMN_NAME_ENTRY_ID, newRowId);
                    layout_values.put(Button.COLUMN_NAME_LAYOUT_PARAMS, i);
                    layout_values.put(Button.COLUMN_NAME_LAYOUT_VALUE, rule);
                    newRowId = db.insert(
                            Button.TABLE_NAME_LAYOUT,
                            null,
                            layout_values);
                    i++;
                }
            }
        }

        public void saveCommand(Command command)
        {
            try (SQLiteDatabase db = getWritableDatabase()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_TEXT,command.getCommand_name());
                values.put(COLUMN_NAME_COMMAND_TYPE,command.getType());
                values.put(COLUMN_NAME_COMMAND,command.getCommand());
                long newRowId;
                newRowId = db.insert(
                        Button.TABLE_NAME_COMMANDS,
                        null,
                        values);
            }
        }

        public List<Command> getCommand(String command_name)
        {
            List<Command> commands = new ArrayList<>();
            try (SQLiteDatabase db = getReadableDatabase()) {
                String[] projection = {
                        Button.COLUMN_NAME_TEXT,
                        Button.COLUMN_NAME_COMMAND_TYPE,
                        COLUMN_NAME_COMMAND

                };
                String selection = null;
                String[] selectionArgs = null;
                if(command_name != null)
                {
                    selection = Button.COLUMN_NAME_TEXT + " LIKE ?";
                    selectionArgs = new String[]{command_name};
                }
                try(Cursor c = db.query(Button.TABLE_NAME_COMMANDS,projection,selection,selectionArgs,null,null,null))
                {
                    while(c.moveToNext())
                    {
                        Command command = new Command(c.getString(c.getColumnIndex(Button.COLUMN_NAME_TEXT)),c.getString(c.getColumnIndex(Button.COLUMN_NAME_COMMAND)),null,c.getString(c.getColumnIndex(Button.COLUMN_NAME_COMMAND_TYPE)));
                        commands.add(command);
                    }
                }
            }
            return commands;
        }

        public void saveBtnCommand(String command_name,String id)
        {
            if(command_name == null)
            {
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
                            Button.TABLE_NAME_COMMANDS_BTN,
                            null,
                            values);
                }
            }
        }

        public List<Command> getBtnCommands(String id)
        {
            List<Command> commands = new ArrayList<>();
            try (SQLiteDatabase db = getReadableDatabase()) {
                String[] projection = {
                        Button.COLUMN_NAME_ENTRY_ID,
                        Button.COLUMN_NAME_TEXT,
                        Button.COLUMN_NAME_COMMAND_TYPE,
                        COLUMN_NAME_COMMAND

                };
                String selection = null;
                String[] selectionArgs = null;
                selection = Button.COLUMN_NAME_ENTRY_ID + " LIKE ?";
                selectionArgs = new String[]{id};

                try(Cursor c = db.query(Button.TABLE_NAME_COMMANDS_BTN,projection,selection,selectionArgs,null,null,null))
                {
                    while(c.moveToNext())
                    {
                        Command command = new Command(c.getString(c.getColumnIndex(Button.COLUMN_NAME_TEXT)),c.getString(c.getColumnIndex(Button.COLUMN_NAME_COMMAND)),String.valueOf(c.getInt(c.getColumnIndex(Button.COLUMN_NAME_ENTRY_ID))),c.getString(c.getColumnIndex(Button.COLUMN_NAME_COMMAND_TYPE)));
                        commands.add(command);
                    }
                }
            }
            return commands;
        }



        public List<android.widget.Button> getButtons(String tab,Context cntx) {

            List<android.widget.Button> buttonList = new ArrayList<>();
            SQLiteDatabase db = getReadableDatabase();
            String[] projection = {
                    Button._ID,
                    COLUMN_NAME_TEXT,
                    Button.COLUMN_NAME_COORD_X,
                    Button.COLUMN_NAME_COORD_Y,
                    Button.COLUMN_NAME_TAB,
                    Button.COLUMN_NAME_WIDTH,
                    Button.COLUMN_NAME_HEIGH
            };

            // TODO do with tab like !!!!!
            String selection_btn = Button.COLUMN_NAME_TAB + " LIKE ?";
            String[] selectionArgs_btn = { tab };
            try (Cursor c = db.query(Button.TABLE_NAME_BUTTON, projection, selection_btn, selectionArgs_btn, null, null, null)) {
                while(c.moveToNext())
                {
                    android.widget.Button btn = new android.widget.Button(cntx);
                    btn.setId(c.getInt(c.getColumnIndex(Button._ID)));
                    btn.setText(c.getString(c.getColumnIndex(COLUMN_NAME_TEXT)));
                    btn.setY(c.getFloat(c.getColumnIndex(Button.COLUMN_NAME_COORD_Y)));
                    btn.setX(c.getFloat(c.getColumnIndex(Button.COLUMN_NAME_COORD_X)));
                    btn.setWidth(c.getInt(c.getColumnIndex(Button.COLUMN_NAME_WIDTH)));
                    btn.setHeight(c.getInt(c.getColumnIndex(Button.COLUMN_NAME_HEIGH)));
                    buttonList.add(btn);
                }

            }

            String[] projection_l = {
                    Button.COLUMN_NAME_ENTRY_ID,
                    Button.COLUMN_NAME_LAYOUT_PARAMS,
                    Button.COLUMN_NAME_LAYOUT_VALUE
            };
            String selection = Button.COLUMN_NAME_ENTRY_ID + " LIKE ?";

            for(android.widget.Button button : buttonList)
            {


                String[] selectionArgs = { String.valueOf(button.getId()) };

                try(Cursor c = db.query(Button.TABLE_NAME_LAYOUT, projection_l,selection,selectionArgs,null,null,null))
                {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                    while (c.moveToNext())
                    {
                        layoutParams.addRule(c.getInt(c.getColumnIndex(Button.COLUMN_NAME_LAYOUT_PARAMS)),c.getInt(c.getColumnIndex(Button.COLUMN_NAME_LAYOUT_VALUE)));
                    }
                    button.setLayoutParams(layoutParams);
                };

            }
            db.close();
            return buttonList;
        }

        public void removeButton(String id)
        {
            Log.d("DB","!!!!!!!!!!!!!!!!!!!!!remove BUTTON ID: " + id);
            try (SQLiteDatabase db = getWritableDatabase()) {
                String selection = Button._ID + " LIKE ?";
                String[] selectionArgs = { id};
                db.delete(Button.TABLE_NAME_BUTTON,selection,selectionArgs);
                db.delete(Button.TABLE_NAME_LAYOUT,selection,selectionArgs);
            }

        }

        public void removeCommandBtn(String id,String commandName)
        {
            try(SQLiteDatabase db = getWritableDatabase())
            {
                String selelection = Button.COLUMN_NAME_ENTRY_ID + " LIKE ? AND " + Button.COLUMN_NAME_TEXT + " LIKE ?";
                String[] selectionArgs = {id,commandName};
                db.delete(Button.TABLE_NAME_COMMANDS_BTN,selelection,selectionArgs);
            }
        }

    }

}
