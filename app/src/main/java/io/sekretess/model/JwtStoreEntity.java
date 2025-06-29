package io.sekretess.model;

import android.provider.BaseColumns;

public class JwtStoreEntity implements BaseColumns {
    public static final String TABLE_NAME = "jwt_store";
    public static final String COLUMN_JWT = "jwt";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            _ID + " INTEGER PRIMARY KEY, " +
            COLUMN_JWT + " TEXT," +
            COLUMN_CREATED_AT + " TEXT)";
    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
