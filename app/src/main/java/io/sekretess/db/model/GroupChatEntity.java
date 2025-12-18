package io.sekretess.db.model;

import android.provider.BaseColumns;

public class GroupChatEntity implements BaseColumns {
    public static final String TABLE_NAME = "group_chat_store";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_DISTRIBUTION_KEY = "dist_key";

    public static final String COLUMN_CREATED_AT = "created_at";

    public final static String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" + _ID + " INTEGER PRIMARY KEY," +
            COLUMN_SENDER + " TEXT," +
            COLUMN_DISTRIBUTION_KEY + " TEXT," +
            COLUMN_CREATED_AT + " TEXT)";

    public final static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
