package io.sekretess.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.IdentityKeyStore;

import java.time.Instant;
import java.util.Base64;

import io.sekretess.model.IdentityKeyEntity;
import io.sekretess.model.IdentityKeyPairStoreEntity;

public class IdentityKeyRepository {

    private final DbHelper dbHelper;
    private final String TAG = IdentityKeyRepository.class.getName();

    public IdentityKeyRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }


    public IdentityKeyPair getIdentityKeyPair() {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(IdentityKeyPairStoreEntity.TABLE_NAME,
                new String[]{IdentityKeyPairStoreEntity._ID, IdentityKeyPairStoreEntity.COLUMN_IKP},
                null, null, null, null, null)) {
            if (cursor.moveToNext()) {
                String ikp = cursor.getString(cursor.getColumnIndexOrThrow(IdentityKeyPairStoreEntity.COLUMN_IKP));
                return new IdentityKeyPair(DbHelper.base64Decoder.decode(ikp));
            }
        }
        return null;
    }

    public void storeIdentityKeyPair(IdentityKeyPair identityKeyPair) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IdentityKeyPairStoreEntity.COLUMN_IKP,
                DbHelper.base64Encoder.encodeToString(identityKeyPair.serialize()));
        contentValues.put(IdentityKeyPairStoreEntity.COLUMN_CREATED_AT, DbHelper.dateTimeFormatter.format(Instant.now()));
        dbHelper.getWritableDatabase().insert(IdentityKeyPairStoreEntity.TABLE_NAME, null, contentValues);
    }

    public IdentityKeyStore.IdentityChange saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        IdentityKey trustedKey = getIdentity(address);
        if (trustedKey == null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(IdentityKeyEntity.COLUMN_ADDRESS_DEVICE_ID, address.getDeviceId());
            contentValues.put(IdentityKeyEntity.COLUMN_ADDRESS_NAME, address.getName());
            contentValues.put(IdentityKeyEntity.COLUMN_IDENTITY_KEY, DbHelper.base64Encoder
                    .encodeToString(identityKey.serialize()));
            dbHelper.getWritableDatabase().insert(IdentityKeyEntity.TABLE_NAME, null, contentValues);
            return IdentityKeyStore.IdentityChange.NEW_OR_UNCHANGED;
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(IdentityKeyEntity.COLUMN_IDENTITY_KEY, DbHelper.base64Encoder
                    .encodeToString(identityKey.serialize()));
            dbHelper.getWritableDatabase().update(IdentityKeyEntity.TABLE_NAME, contentValues,
                    IdentityKeyEntity.COLUMN_ADDRESS_DEVICE_ID + " = ? AND " + IdentityKeyEntity.COLUMN_ADDRESS_NAME + " = ?",
                    new String[]{String.valueOf(address.getDeviceId()), address.getName()});
            return IdentityKeyStore.IdentityChange.REPLACED_EXISTING;
        }
    }

    public IdentityKey getIdentity(SignalProtocolAddress address) {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(IdentityKeyEntity.TABLE_NAME,
                new String[]{IdentityKeyEntity._ID, IdentityKeyEntity.COLUMN_IDENTITY_KEY},
                IdentityKeyEntity.COLUMN_ADDRESS_DEVICE_ID + " = ? AND " + IdentityKeyEntity.COLUMN_ADDRESS_NAME + " = ?",
                new String[]{String.valueOf(address.getDeviceId()), address.getName()}, null, null, null)) {
            while (cursor.moveToNext()) {
                return new IdentityKey(DbHelper.base64Decoder.decode(cursor
                        .getString(cursor.getColumnIndexOrThrow(IdentityKeyEntity.COLUMN_IDENTITY_KEY))));
            }
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Error occurred while getting identity key", e);
        }
        return null;
    }
}
