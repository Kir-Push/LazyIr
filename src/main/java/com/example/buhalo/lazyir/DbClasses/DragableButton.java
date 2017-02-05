package com.example.buhalo.lazyir.DbClasses;

import android.provider.BaseColumns;

/**
 * Created by buhalo on 05.02.17.
 */

public final class DragableButton {

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
    }
}
