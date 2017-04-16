package com.tuncay.superlotteryluckynumbers.db;

import android.provider.BaseColumns;

/**
 * Created by mac on 4.01.2017.
 */
public final class LotteryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private LotteryContract() {}

    /* Inner class that defines the table contents */
    public static class LotteryEntry implements BaseColumns {
        public static final String TABLE_NAME = "Coupons";
        public static final String COLUMN_NAME_ID = "CouponId";
        public static final String COLUMN_NAME_USER = "User";
        public static final String COLUMN_NAME_GAME_TYPE = "GameType";
        public static final String COLUMN_NAME_NUMS = "Numbers";
        public static final String COLUMN_NAME_PLAY_TIME = "PlayTime";
        public static final String COLUMN_NAME_LOTTERY_TIME = "LotteryTime";
        public static final String COLUMN_NAME_TO_REMIND = "ToRemind";
        public static final String COLUMN_NAME_SERVER_CALLED = "ServerCalled";
        public static final String COLUMN_NAME_WIN_COUNT = "WinCount";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + LotteryEntry.TABLE_NAME + " (" +
                        LotteryEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        LotteryEntry.COLUMN_NAME_USER + " TEXT," +
                        LotteryEntry.COLUMN_NAME_GAME_TYPE + " TEXT," +
                        LotteryEntry.COLUMN_NAME_NUMS + " TEXT," +
                        LotteryEntry.COLUMN_NAME_PLAY_TIME + " DATE," +
                        LotteryEntry.COLUMN_NAME_LOTTERY_TIME + " DATE," +
                        LotteryEntry.COLUMN_NAME_TO_REMIND + " TEXT," +
                        LotteryEntry.COLUMN_NAME_SERVER_CALLED + " TEXT," +
                        LotteryEntry.COLUMN_NAME_WIN_COUNT + " INTEGER)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + LotteryEntry.TABLE_NAME;
    }



}
