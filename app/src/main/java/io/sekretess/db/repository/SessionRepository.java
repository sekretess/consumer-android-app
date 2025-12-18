package io.sekretess.db.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.SessionRecord;

import java.util.ArrayList;
import java.util.List;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.model.SessionStoreEntity;

public class SessionRepository {
    private final String TAG = SessionRepository.class.getName();
    private final SekretessDatabase sekretessDatabase;


    public SessionRepository(SekretessDatabase sekretessDatabase) {
        this.sekretessDatabase = sekretessDatabase;
    }

    public void removeSession(SignalProtocolAddress address) {
        try (SQLiteDatabase db = sekretessDatabase.getWritableDatabase()) {
            db.delete(SessionStoreEntity.TABLE_NAME,
                    SessionStoreEntity.COLUMN_ADDRESS_NAME + " = ? AND "
                            + SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID + " = ?",
                    new String[]{address.getName(), String.valueOf(address.getDeviceId())});
        }
    }

    public void removeAllSessions(String name) {
        try (SQLiteDatabase db = sekretessDatabase.getWritableDatabase()) {
            db.delete(SessionStoreEntity.TABLE_NAME,
                    SessionStoreEntity.COLUMN_ADDRESS_NAME + " = ?", new String[]{name});
        }
    }

    public List<SessionRecord> loadExistingSessions(List<SignalProtocolAddress> addresses) {
        List<SessionRecord> sessionRecords = new ArrayList<>();
        for (SignalProtocolAddress address : addresses) {
            SessionRecord sessionRecord = loadSession(address);
            if (sessionRecord != null) {
                sessionRecords.add(sessionRecord);
            }
        }
        return sessionRecords;
    }

    public SessionRecord loadSession(SignalProtocolAddress address) {
        try (Cursor cursor = sekretessDatabase.getReadableDatabase()
                .query(SessionStoreEntity.TABLE_NAME, new String[]{SessionStoreEntity.COLUMN_SESSION},
                        SessionStoreEntity.COLUMN_ADDRESS_NAME + " = ? AND "
                                + SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID + " = ?",
                        new String[]{address.getName(), String.valueOf(address.getDeviceId())},
                        null, null, null)) {
            while (cursor.moveToNext()) {
                String sessionBase64Str = cursor.getString(cursor.getColumnIndexOrThrow(SessionStoreEntity.COLUMN_SESSION));
                return new SessionRecord(SekretessDatabase.base64Decoder.decode(sessionBase64Str));
            }
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error occurred during load session.", e);
        }
        return null;
    }

    public List<Integer> getSubDeviceSessions(String name) {
        try (Cursor cursor = sekretessDatabase.getReadableDatabase()
                .query(SessionStoreEntity.TABLE_NAME, new String[]{SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID},
                        SessionStoreEntity.COLUMN_ADDRESS_NAME + " = ?", new String[]{name},
                        null, null, null)) {
            List<Integer> deviceIds = new ArrayList<>();
            while (cursor.moveToNext()) {
                int deviceId = cursor.getInt(cursor.getColumnIndexOrThrow(SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID));
                if (deviceId != 1)
                    deviceIds.add(deviceId);
            }
            return deviceIds;
        }
    }

    public boolean containsSession(SignalProtocolAddress address) {
        return loadSession(address) != null;
    }


    public void storeSession(SignalProtocolAddress address, SessionRecord sessionRecord) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SessionStoreEntity.COLUMN_ADDRESS_NAME, address.getName());
        contentValues.put(SessionStoreEntity.COLUMN_ADDRESS_DEVICE_ID, address.getDeviceId());
        if (address.getServiceId() != null) {
            contentValues.put(SessionStoreEntity.COLUMN_SERVICE_ID,
                    SekretessDatabase.base64Encoder.encodeToString(address.getServiceId().toServiceIdBinary()));
        }
        contentValues.put(SessionStoreEntity.COLUMN_SESSION,
                SekretessDatabase.base64Encoder.encodeToString(sessionRecord.serialize()));

        sekretessDatabase.getWritableDatabase().insert(SessionStoreEntity.TABLE_NAME, null, contentValues);


    }

    public void clearStorage() {
        sekretessDatabase.getWritableDatabase().delete(SessionStoreEntity.TABLE_NAME, null, null);
    }
}
