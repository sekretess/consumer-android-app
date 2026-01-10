package io.sekretess.db.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.PreKeyDao;
import io.sekretess.db.model.PreKeyRecordEntity;
import io.sekretess.db.model.SignedPreKeyRecordEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class PreKeyRepository {
    private final PreKeyDao preKeyDao;
    private final String TAG = PreKeyRepository.class.getName();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();


    public PreKeyRepository() {
        SekretessDatabase sekretessDatabase = SekretessDatabase
                .getInstance(SekretessDependencyProvider.applicationContext());
        preKeyDao = sekretessDatabase.preKeyDao();
    }


    public int count() {
        return preKeyDao.getCount();
    }

    public void removePreKeyRecord(int prekeyId) {
        preKeyDao.removePreKeyRecord(prekeyId);
    }

    public void storePreKeyRecord(PreKeyRecord preKeyRecord) {
        PreKeyRecordEntity preKeyRecordEntity = new PreKeyRecordEntity(preKeyRecord.getId(),
                base64Encoder.encodeToString(preKeyRecord.serialize()), false, System.currentTimeMillis());
        preKeyDao.insert(preKeyRecordEntity);
    }

    public PreKeyRecord loadPreKey(int preKeyId) {
        PreKeyRecordEntity preKeyRecordEntity = preKeyDao.getPreKey(preKeyId);
        Log.i(TAG, "preKeyRecordEntity: " + preKeyRecordEntity + " preKeyId:" + preKeyId);
        if (preKeyRecordEntity == null) {
            Log.e(TAG, "PreKeyRecord not found. preKeyId: " + preKeyId);
            return null;
        }

        try {
            return new PreKeyRecord(base64Decoder.decode(preKeyRecordEntity.getPreKeyRecord()));
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error occurred during loadPreKey", e);
            return null;
        }
    }

    public void clearStorage() {
        preKeyDao.clear();
    }
}
