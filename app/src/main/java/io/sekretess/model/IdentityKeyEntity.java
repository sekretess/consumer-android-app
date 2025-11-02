package io.sekretess.model;

import android.provider.BaseColumns;

public class IdentityKeyEntity implements BaseColumns {

    public static final String TABLE_NAME = "identity_key_store";
    public static final String COLUMN_IDENTITY_KEY = "identity_key";
    public static final String COLUMN_ADDRESS_NAME = "address_name";
    public static final String COLUMN_ADDRESS_DEVICE_ID = "address_device_id";
    public static final String COLUMN_CREATED_AT = "created_at";


    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_ADDRESS_DEVICE_ID + " INTEGER," +
            COLUMN_ADDRESS_NAME + " TEXT," +
            COLUMN_IDENTITY_KEY + " TEXT," +
            COLUMN_CREATED_AT + " TEXT)";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
