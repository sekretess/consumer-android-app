package com.sekretess.model;

import android.provider.BaseColumns;

public class IdentityKeyPairStoreEntity implements BaseColumns {
    public static final String TABLE_NAME = "ikp_store";
    public static final String COLUMN_IKP = "ikp";
    public static final String COLUMN_CREATED_AT = "created_at";

    public final static String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
            "(" + _ID + " INTEGER PRIMARY KEY," +
            COLUMN_IKP + " TEXT," +
            COLUMN_CREATED_AT + " TEXT)";

    public final static String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

}
