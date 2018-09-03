package com.example.buhalo.lazyir.db;

import android.provider.BaseColumns;

/**
 * Created by buhalo on 28.02.17.
 */

public interface DbCommands extends BaseColumns {
     String TABLE_NAME_BUTTON = "button";
     String TABLE_NAME_LAYOUT = "layout";
     String TABLE_NAME_COMMANDS = "commands";
     String TABLE_NAME_COMMANDS_BTN = "commandsbtn";
     String TABLE_NAME_PAIRED_DEVICES = "pairedDevices";
     String TABLE_NAME_MODULE = "module";
     String COLUMN_NAME_ENTRY_ID = "entryid";
     String COLUMN_NAME_COMMAND = "command";
     String COLUMN_NAME_COMMAND_TYPE = "type";
     String COLUMN_NAME_TEXT = "text";
     String COLUMN_NAME_DVID = "dvid";
     String COLUMN_NAME_PAIRCODE = "paircode";
     String COLUMN_NAME_COORD_X = "x";
     String COLUMN_NAME_COORD_Y = "y";
     String COLUMN_NAME_RES_ID = "resid";
     String COLUMN_NAME_TAB = "tab";
     String COLUMN_NAME_WIDTH = "width";
     String COLUMN_NAME_HEIGH = "heigh";
     String COLUMN_NAME_LAYOUT_PARAMS = "layoutpar";
     String COLUMN_NAME_LAYOUT_VALUE = "value";
     String COLUMN_NAME_COMAMND_VENDOR = "producer";
     String COLUMT_NAME_COMMAND_DEVICE = "device";
     String COLUMN_NAME_MODULE_NAME = "name";
     String COLUMN_NAME_MODULE_DEVICE = "device";
     String COLUMN_NAME_MODULE_STATUS = "status";
     String COLUMN_NAME_MODULE_OPTION = "option";
     String COLUMN_NAME_MODULE_LIMIT = "limit";
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

     String SQL_CREATE_ENTRIES_COMMANDS =
            "CREATE TABLE " + TABLE_NAME_COMMANDS + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_COMAMND_VENDOR + TEXT_TYPE + COMMA_SEP +
                    COLUMT_NAME_COMMAND_DEVICE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMMAND_TYPE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMMAND + TEXT_TYPE + ")";

     String SQL_CREATE_ENTRIES_BUTTON_COMMANDS =
            "CREATE TABLE " + TABLE_NAME_COMMANDS_BTN + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_ENTRY_ID + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMAMND_VENDOR + TEXT_TYPE + COMMA_SEP +
                    COLUMT_NAME_COMMAND_DEVICE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMMAND_TYPE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMMAND + TEXT_TYPE + ")";

     String SQL_CREATE_PAIRED_DEVICES =
             "CREATE TABLE " + TABLE_NAME_PAIRED_DEVICES + " (" +
                     _ID + " INTEGER PRIMARY KEY," +
                     COLUMN_NAME_DVID + TEXT_TYPE + COMMA_SEP +
                     COLUMN_NAME_PAIRCODE + TEXT_TYPE + ")";

     String SQL_DELETE_ENTRIES_BUTTON =
            "DROP TABLE IF EXISTS " + TABLE_NAME_BUTTON;

     String SQL_DELETE_ENTRIES_LAYOUT =
            "DROP TABLE IF EXISTS " + TABLE_NAME_LAYOUT;

     String SQL_DELETE_ENTRIES_COMMANDS =
            "DROP TABLE IF EXISTS " + TABLE_NAME_COMMANDS;

     String SQL_DELETE_ENTRIES_COMMANDS_BTN =
            "DROP TABLE IF EXISTS " + TABLE_NAME_COMMANDS_BTN;

     String SQL_DELETE_PAIRED_DEVICES =
             "DROP TABLE IF EXISTS " + TABLE_NAME_PAIRED_DEVICES;
}
