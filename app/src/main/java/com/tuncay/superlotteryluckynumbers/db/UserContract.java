package com.tuncay.superlotteryluckynumbers.db;

import android.provider.BaseColumns;

/**
 * Created by mac on 6.04.2017.
 */
public class UserContract implements BaseColumns{
    private UserContract() {}

    /* Inner class that defines the table contents */
    public static class UserEntry {

        public static final String TABLE_NAME = "Users";
        public static final String COLUMN_NAME_ID = "UserId";
        public static final String COLUMN_NAME_USER = "User";
        public static final String COLUMN_NAME_TOKEN = "Token";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                        UserEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        UserEntry.COLUMN_NAME_USER + " TEXT," +
                        UserEntry.COLUMN_NAME_TOKEN + " TEXT)";


        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;
    }
}
