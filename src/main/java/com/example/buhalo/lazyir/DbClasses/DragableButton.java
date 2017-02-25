package com.example.buhalo.lazyir.DbClasses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.widget.Button;

import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_CREATE_ENTRIES;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_DELETE_ENTRIES;

/**
 * Created by buhalo on 05.02.17.
 */

public  class DragableButton {

    public DragableButton(){}

    public static abstract class Button implements BaseColumns
    {
        public static final String TABLE_NAME = "button";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_COORD_X = "x";
        public static final String COLUMN_NAME_COORD_Y = "y";
        public static final String COLUMN_NAME_TAB = "tab";
        public static final String COLUMN_NAME_WIDTH = "width";
        public static final String COLUMN_NAME_HEIGH = "heigh";
        public static final String COLUMN_NAME_LAYOUT_PARAMS = "layoutpar";
        public static final String TEXT_TYPE = " TEXT";
        public static final String COMMA_SEP = ",";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Button.TABLE_NAME + " (" +
                        Button._ID + " INTEGER PRIMARY KEY," +
                        Button.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                        Button.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_COORD_X + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_COORD_Y + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_TAB + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_WIDTH + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_HEIGH + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_LAYOUT_PARAMS + TEXT_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Button.TABLE_NAME;
    }

    public class DbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Buttons.db";

        public DbHelper(Context context) {
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }



}
