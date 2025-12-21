package io.sekretess.db.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.SignedPreKeyDao;
import io.sekretess.db.model.SignedPreKeyRecordEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class SignedPreKeyRepository {
    private SignedPreKeyDao signedPreKeyDao;
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();
    private final String TAG = SignedPreKeyRepository.class.getName();

    public SignedPreKeyRepository() {
        SekretessDatabase sekretessDatabase = SekretessDatabase
                .getInstance(SekretessDependencyProvider.applicationContext());
        signedPreKeyDao = sekretessDatabase.signedPreKeyDao();
    }

    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        return signedPreKeyDao.getAll()
                .stream()
                .map(signedPreKeyRecordEntity -> {
                    try {
                        return new SignedPreKeyRecord(base64Decoder.decode(signedPreKeyRecordEntity.getSpkRecord()));
                    } catch (InvalidMessageException e) {
                        Log.e(TAG, "Error occurred during get spk from database", e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .toList();
    }

    public SignedPreKeyRecord getSignedPreKeyRecord(int signedPreKeyId) {
        SignedPreKeyRecordEntity signedPreKeyRecordEntity = signedPreKeyDao.getSignedPreKeyRecord(signedPreKeyId);
        if (signedPreKeyRecordEntity == null) {
            return null;
        }

        try {
            return new SignedPreKeyRecord(base64Decoder.decode(signedPreKeyRecordEntity.getSpkRecord()));
        } catch (InvalidMessageException e) {
            Log.e(TAG, "Error occurred during get spk from database", e);
            return null;
        }
    }

    public void removeSignedPreKey(int signedPreKeyId) {
        signedPreKeyDao.removeSignedPreKey(signedPreKeyId);
    }

    public void storeSignedPreKeyRecord(SignedPreKeyRecord signedPreKeyRecord) {
        SignedPreKeyRecordEntity signedPreKeyRecordEntity =
                new SignedPreKeyRecordEntity(base64Encoder
                        .encodeToString(signedPreKeyRecord.serialize()),
                        signedPreKeyRecord.getId(), System.currentTimeMillis());
        signedPreKeyDao.insert(signedPreKeyRecordEntity);
    }
}
