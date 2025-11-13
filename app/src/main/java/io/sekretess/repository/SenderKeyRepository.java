package io.sekretess.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;

import java.util.UUID;

import io.sekretess.model.SenderKeyEntity;

public class SenderKeyRepository {

    private final DbHelper dbHelper;
    private final String TAG = SenderKeyRepository.class.getName();

    public SenderKeyRepository(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SenderKeyEntity.COLUMN_ADDRESS_NAME, sender.getName());
        contentValues.put(SenderKeyEntity.COLUMN_ADDRESS_DEVICE_ID, sender.getDeviceId());
        contentValues.put(SenderKeyEntity.COLUMN_DISTRIBUTION_UUID, distributionId.toString());
        contentValues.put(SenderKeyEntity.COLUMN_SENDER_KEY_RECORD, DbHelper.base64Encoder.encodeToString(record.serialize()));
        dbHelper.getWritableDatabase().insert(SenderKeyEntity.TABLE_NAME, null, contentValues);
    }

    public SenderKeyRecord loadSenderKey(SignalProtocolAddress sender, UUID distributionId) {
        try (Cursor cursor = dbHelper.getReadableDatabase().query(SenderKeyEntity.TABLE_NAME,
                new String[]{SenderKeyEntity._ID, SenderKeyEntity.COLUMN_SENDER_KEY_RECORD,
                        SenderKeyEntity.COLUMN_ADDRESS_DEVICE_ID, SenderKeyEntity.COLUMN_ADDRESS_NAME},
                SenderKeyEntity.COLUMN_ADDRESS_NAME + " = ? AND "
                        + SenderKeyEntity.COLUMN_ADDRESS_DEVICE_ID + " = ? AND "
                        + SenderKeyEntity.COLUMN_DISTRIBUTION_UUID + " = ?",
                new String[]{sender.getName(), String.valueOf(sender.getDeviceId()), distributionId.toString()},
                null, null, null)) {
            while (cursor.moveToNext()) {
                return new SenderKeyRecord(DbHelper.base64Decoder.decode(cursor.getString(1)));
            }
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error occurred while getting SenderKeyRecord", e);
        }
        return null;
    }

    public void clearStorage() {
        dbHelper.getWritableDatabase().delete(SenderKeyEntity.TABLE_NAME, null, null);
    }
}
