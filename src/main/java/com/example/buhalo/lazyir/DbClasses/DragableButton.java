package com.example.buhalo.lazyir.DbClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_CREATE_ENTRIES_BUTTON;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_CREATE_ENTRIES_LAYOUT;
import static com.example.buhalo.lazyir.DbClasses.DragableButton.Button.SQL_DELETE_ENTRIES_BUTTON;
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
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
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
                        Button.COLUMN_NAME_ENTRY_ID + INTEGER_TYPE + COMMA_SEP +
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



        public static final String SQL_DELETE_ENTRIES_BUTTON =
                "DROP TABLE IF EXISTS " + Button.TABLE_NAME_BUTTON;

        public static final String SQL_DELETE_ENTRIES_LAYOUT =
                "DROP TABLE IF EXISTS " + Button.TABLE_NAME_LAYOUT;
    }

    public class DbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Buttons.db";

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_BUTTON);
            db.execSQL(SQL_CREATE_ENTRIES_LAYOUT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES_BUTTON);
            db.execSQL(SQL_DELETE_ENTRIES_LAYOUT);
            onCreate(db);
        }


        public void saveButton(android.widget.Button button, String tab) {
            if (button == null) {
                //TODO
            }
            try (SQLiteDatabase db = getWritableDatabase()) {
                ContentValues values = new ContentValues();
                ContentValues layout_values = new ContentValues();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
                values.put(Button.COLUMN_NAME_ENTRY_ID, button.getId());
                values.put(Button.COLUMN_NAME_TEXT, button.getText().toString());
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
                int i = 0;
                for (int rule : params.getRules()) {
                    layout_values.put(Button.COLUMN_NAME_ENTRY_ID, button.getId());
                    layout_values.put(Button.COLUMN_NAME_LAYOUT_PARAMS, i);
                    layout_values.put(Button.COLUMN_NAME_LAYOUT_VALUE, rule);
                    newRowId = db.insert(
                            Button.TABLE_NAME_LAYOUT,
                            null,
                            values);
                    i++;
                }
            }

        }

        public List<android.widget.Button> getButtons(String tab,Context cntx) {

            List<android.widget.Button> buttonList = new ArrayList<>();
            SQLiteDatabase db = getReadableDatabase();
            String[] projection = {
                    Button.COLUMN_NAME_ENTRY_ID,
                    Button.COLUMN_NAME_TEXT,
                    Button.COLUMN_NAME_COORD_X,
                    Button.COLUMN_NAME_COORD_Y,
                    Button.COLUMN_NAME_TAB,
                    Button.COLUMN_NAME_WIDTH,
                    Button.COLUMN_NAME_HEIGH
            };

            // TODO do with tab like !!!!!
            try (Cursor c = db.query(Button.TABLE_NAME_BUTTON, projection, null, null, null, null, null)) {
                while(c.moveToNext())
                {
                    android.widget.Button btn = new android.widget.Button(cntx);
                    btn.setId(c.getInt(c.getColumnIndex(Button.COLUMN_NAME_ENTRY_ID)));
                    btn.setText(c.getString(c.getColumnIndex(Button.COLUMN_NAME_TEXT)));
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
                        layoutParams.addRule(c.getInt(c.getColumnIndex(Button.COLUMN_NAME_LAYOUT_PARAMS)));
                        layoutParams.addRule(c.getInt(c.getColumnIndex(Button.COLUMN_NAME_LAYOUT_VALUE)));
                    }
                    button.setLayoutParams(layoutParams);
                };

            }
            return buttonList;
        }

    }

}
