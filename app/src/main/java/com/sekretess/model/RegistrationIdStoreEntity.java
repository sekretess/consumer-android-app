package com.sekretess.model;

import android.provider.BaseColumns;

public class RegistrationIdStoreEntity implements BaseColumns {

    public static final String TABLE_NAME = "registration_id_store";
    public static final String COLUMN_REG_ID = "reg_id";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            "(" +
            _ID + " INTEGER PRIMARY KEY, " +
            COLUMN_REG_ID + " TEXT," +
            COLUMN_CREATED_AT + " TEXT)";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
