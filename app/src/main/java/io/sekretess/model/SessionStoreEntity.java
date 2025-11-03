package io.sekretess.model;

import android.provider.BaseColumns;

public class SessionStoreEntity implements BaseColumns {

    public static final String TABLE_NAME = "session_store";
    public static final String COLUMN_SESSION = "session";
    public static final String COLUMN_ADDRESS_NAME = "address_name";

    public static final String COLUMN_ADDRESS_DEVICE_ID = "address_device_id";
    public static final String COLUMN_SERVICE_ID = "service_id";
    public static final String COLUMN_CREATED_AT = "created_at";


    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_ADDRESS_DEVICE_ID + " INTEGER," +
            COLUMN_ADDRESS_NAME + " TEXT," +
            COLUMN_SERVICE_ID + " TEXT," +
            COLUMN_SESSION + " TEXT," +
            COLUMN_CREATED_AT + " TEXT)";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
