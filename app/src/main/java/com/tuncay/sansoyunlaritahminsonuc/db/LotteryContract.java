package com.tuncay.sansoyunlaritahminsonuc.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.sql.Date;

/**
 * Created by mac on 4.01.2017.
 */
public final class LotteryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private LotteryContract() {}

    /* Inner class that defines the table contents */
    public static class LotteryEntry implements BaseColumns {
        public static final String TABLE_NAME = "LotteryNumbers";
        public static final String COLUMN_NAME_USER = "User";
        public static final String COLUMN_NAME_GAME_TYPE = "GameType";
        public static final String COLUMN_NAME_NUM1 = "Num1";
        public static final String COLUMN_NAME_NUM2 = "Num2";
        public static final String COLUMN_NAME_NUM3 = "Num3";
        public static final String COLUMN_NAME_NUM4 = "Num4";
        public static final String COLUMN_NAME_NUM5 = "Num5";
        public static final String COLUMN_NAME_NUM6 = "Num6";
        public static final String COLUMN_NAME_PLAY_TIME = "PlayTime";
        public static final String COLUMN_NAME_LOTTERY_TIME = "LotteryTime";
        public static final String COLUMN_NAME_TO_REMIND = "ToRemind";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + LotteryEntry.TABLE_NAME + " (" +
                        LotteryEntry._ID + " INTEGER PRIMARY KEY," +
                        LotteryEntry.COLUMN_NAME_USER + " TEXT," +
                        LotteryEntry.COLUMN_NAME_GAME_TYPE + " TEXT," +
                        LotteryEntry.COLUMN_NAME_NUM1 + " INTEGER," +
                        LotteryEntry.COLUMN_NAME_NUM2 + " INTEGER," +
                        LotteryEntry.COLUMN_NAME_NUM3 + " INTEGER," +
                        LotteryEntry.COLUMN_NAME_NUM4 + " INTEGER," +
                        LotteryEntry.COLUMN_NAME_NUM5 + " INTEGER," +
                        LotteryEntry.COLUMN_NAME_NUM6 + " INTEGER," +
                        LotteryEntry.COLUMN_NAME_PLAY_TIME + " DATE," +
                        LotteryEntry.COLUMN_NAME_LOTTERY_TIME + " DATE," +
                        LotteryEntry.COLUMN_NAME_TO_REMIND + " TEXT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + LotteryEntry.TABLE_NAME;
    }



}
