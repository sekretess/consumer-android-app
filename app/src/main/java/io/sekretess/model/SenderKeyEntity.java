package io.sekretess.model;

import android.provider.BaseColumns;

public class SenderKeyEntity implements BaseColumns {
    public static final String TABLE_NAME = "sender_key_store";
    public static final String COLUMN_ADDRESS_NAME = "address_name";
    public static final String COLUMN_ADDRESS_DEVICE_ID = "address_device_id";
    public static final String COLUMN_DISTRIBUTION_UUID = "distribution_uuid";
    public static final String COLUMN_SENDER_KEY_RECORD = "sender_key_record";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            _ID + " INTEGER PRIMARY KEY, " +
            COLUMN_ADDRESS_DEVICE_ID + " INTEGER," +
            COLUMN_ADDRESS_NAME + " TEXT," +
            COLUMN_SENDER_KEY_RECORD + " TEXT," +
            COLUMN_DISTRIBUTION_UUID + " TEXT, " +
            COLUMN_CREATED_AT + " TEXT)";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}

