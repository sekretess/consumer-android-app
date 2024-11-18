package com.sekretess.model;

import android.provider.BaseColumns;

public class AuthStateStoreEntity implements BaseColumns {
    public static final String TABLE_NAME = "auth_state_store";
    public static final String COLUMN_AUTH_STATE = "auth_state";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_AUTH_STATE + " TEXT)";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
