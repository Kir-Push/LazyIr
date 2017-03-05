package com.example.buhalo.lazyir.DbClasses;

import android.provider.BaseColumns;

/**
 * Created by buhalo on 28.02.17.
 */

public interface DbCommands extends BaseColumns {
     String TABLE_NAME_BUTTON = "button";
     String TABLE_NAME_LAYOUT = "layout";
     String TABLE_NAME_COMMANDS = "commands";
     String TABLE_NAME_COMMANDS_BTN = "commandsbtn";
     String COLUMN_NAME_ENTRY_ID = "entryid";
     String COLUMN_NAME_COMMAND = "command";
     String COLUMN_NAME_COMMAND_TYPE = "type";
     String COLUMN_NAME_TEXT = "text";
     String COLUMN_NAME_COORD_X = "x";
     String COLUMN_NAME_COORD_Y = "y";
     String COLUMN_NAME_RES_ID = "resid";
     String COLUMN_NAME_TAB = "tab";
     String COLUMN_NAME_WIDTH = "width";
     String COLUMN_NAME_HEIGH = "heigh";
     String COLUMN_NAME_LAYOUT_PARAMS = "layoutpar";
     String COLUMN_NAME_LAYOUT_VALUE = "value";
     String TEXT_TYPE = " TEXT";
     String COMMA_SEP = ",";
     String INTEGER_TYPE = " INTEGER";
     String SQL_CREATE_ENTRIES_BUTTON =
            "CREATE TABLE " + TABLE_NAME_BUTTON + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    //      COLUMN_NAME_ENTRY_ID + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COORD_X + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_COORD_Y + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_TAB + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_WIDTH + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_HEIGH + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_RES_ID + TEXT_TYPE +
                    ")";
     String SQL_CREATE_ENTRIES_LAYOUT =
            "CREATE TABLE " + TABLE_NAME_LAYOUT + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_ENTRY_ID + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_LAYOUT_PARAMS + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_LAYOUT_VALUE + INTEGER_TYPE +
                    ")";

     String SQL_CREATE_ENTRIES_COMMANDS = //TODO create unique name index !!!
            "CREATE TABLE " + TABLE_NAME_COMMANDS + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMMAND_TYPE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMMAND + TEXT_TYPE + ")";

     String SQL_CREATE_ENTRIES_BUTTON_COMMANDS =
            "CREATE TABLE " + TABLE_NAME_COMMANDS_BTN + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_ENTRY_ID + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMMAND_TYPE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMMAND + TEXT_TYPE + ")";


     String SQL_DELETE_ENTRIES_BUTTON =
            "DROP TABLE IF EXISTS " + TABLE_NAME_BUTTON;

     String SQL_DELETE_ENTRIES_LAYOUT =
            "DROP TABLE IF EXISTS " + TABLE_NAME_LAYOUT;

     String SQL_DELETE_ENTRIES_COMMANDS =
            "DROP TABLE IF EXISTS " + TABLE_NAME_COMMANDS;

     String SQL_DELETE_ENTRIES_COMMANDS_BTN =
            "DROP TABLE IF EXISTS " + TABLE_NAME_COMMANDS_BTN;
}
