package io.sekretess.db.model;

import android.provider.BaseColumns;

public class SignedPreKeyRecordStoreEntity implements BaseColumns {
    public static final String TABLE_NAME = "signed_prekey_record_store";
    public static final String COLUMN_SPK_RECORD = "spk_record";
    public static final String COLUMN_SPK_ID = "spk_id";
    public static final String COLUMN_CREATED_AT = "created_at";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            _ID + " INTEGER PRIMARY KEY, " +
            COLUMN_SPK_ID + " INTEGER, " +
            COLUMN_SPK_RECORD + " TEXT, " +
            COLUMN_CREATED_AT + " TEXT )";
    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
