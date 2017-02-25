package com.example.buhalo.lazyir;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.buhalo.lazyir.DbClasses.DragableButton;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by buhalo on 05.02.17.
 */

public class DragableButtonTest extends AndroidTestCase {

    @Test
    public void testDbCreation()
    {
        DragableButton dragableButton = new DragableButton();
        DragableButton.DbHelper dragB = dragableButton.new DbHelper(getContext());
        SQLiteDatabase db =  dragB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DragableButton.Button.COLUMN_NAME_ENTRY_ID, 100);
        values.put(DragableButton.Button.COLUMN_NAME_TEXT, "dada");
        long newRowId;
        newRowId = db.insert(
                DragableButton.Button.TABLE_NAME_BUTTON,
                null,
                values);

        String[] projection = {
                DragableButton.Button.COLUMN_NAME_ENTRY_ID,
                DragableButton.Button.COLUMN_NAME_TEXT
        };
        Cursor c = db.query(
                DragableButton.Button.TABLE_NAME_BUTTON,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        c.moveToFirst();
        long itemId = c.getLong(
                c.getColumnIndexOrThrow(DragableButton.Button.COLUMN_NAME_ENTRY_ID)

        );

        assertEquals(100,itemId);
    }

}
