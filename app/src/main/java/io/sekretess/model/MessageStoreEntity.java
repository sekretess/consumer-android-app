package io.sekretess.model;

import android.provider.BaseColumns;

public class MessageStoreEntity implements BaseColumns {
    public static final String TABLE_NAME = "sekretes_message_store";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String COLUMN_MESSAGE_BODY = "message_body";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            _ID + " INTEGER PRIMARY KEY, " +
            COLUMN_SENDER + " TEXT, " +
            COLUMN_USERNAME + " TEXT, " +
            COLUMN_MESSAGE_BODY + " TEXT, " +
            COLUMN_CREATED_AT + " TEXT)";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
