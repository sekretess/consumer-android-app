package com.sekretess.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sekretess.model.SenderEntity;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Sekretess.db";

    private static final String CREATE_SENDERS_ENTITY = "CREATE TABLE " + SenderEntity.TABLE_NAME + "(" +
            SenderEntity._ID +" INTEGER PRIMARY KEY," +
            SenderEntity.COLUMN_NAME_SENDER + " TEXT," +
            SenderEntity.COLUMN_NAME_UNREAD_COUNT +" INTEGER,"+
            SenderEntity.COLUMN_NAME_CREATED_AT + " TEXT)";

    private static final String SQL_DELETE_SENDERS_ENTITY = "DROP TABLE IF EXISTS " + SenderEntity.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SENDERS_ENTITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
