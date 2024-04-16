package com.sekretess.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sekretess.model.MessageEntity;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Sekretess.db";

    private static final String SQL_CREATE_SEKRETES_MESSAGE_ENTITY = "CREATE TABLE " + MessageEntity.TABLE_NAME + "(" +
            MessageEntity._ID + " INTEGER PRIMARY KEY, " +
            MessageEntity.COLUMN_SENDER + " TEXT, " +
            MessageEntity.COLUMN_MESSAGE_BODY + " TEXT, " +
            MessageEntity.COLUMN_CREATED_AT + " TEXT)";

    private static final String SQL_DELETE_SEKRETESS_MESSAGE_ENTITY = "DROP TABLE IF EXISTS " + MessageEntity.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_SEKRETES_MESSAGE_ENTITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_SEKRETESS_MESSAGE_ENTITY);
    }
}
