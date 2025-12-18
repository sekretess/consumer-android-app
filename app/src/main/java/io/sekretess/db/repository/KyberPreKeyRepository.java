package io.sekretess.db.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.KyberPreKeyRecordDao;
import io.sekretess.db.model.KyberPreKeyRecordEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class KyberPreKeyRepository {
    private final KyberPreKeyRecordDao kyberPreKeyRecordDao;
    private final String TAG = KyberPreKeyRepository.class.getName();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();


    public KyberPreKeyRepository() {
        SekretessDatabase sekretessDatabase = SekretessDatabase
                .getInstance(SekretessDependencyProvider.applicationContext());
        this.kyberPreKeyRecordDao = sekretessDatabase.kyberPreKeyRecordDao();
    }


    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        kyberPreKeyRecordDao.markUsed(kyberPreKeyId);
    }

    public void storeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) {
        KyberPreKeyRecordEntity kyberPreKeyRecordEntity
                = new KyberPreKeyRecordEntity(kyberPreKeyRecord.getId(),
                base64Encoder.encodeToString(kyberPreKeyRecord.getSignature()));
        kyberPreKeyRecordDao.insert(kyberPreKeyRecordEntity);
    }

    public KyberPreKeyRecord loadKyberPreKey(int kyberPreKeyId) {
        KyberPreKeyRecordEntity kyberPreKeyRecordEntity = kyberPreKeyRecordDao
                .loadKyberPreKey(kyberPreKeyId);
        try {
            if (kyberPreKeyRecordEntity != null) {
                return new KyberPreKeyRecord(base64Decoder.decode(kyberPreKeyRecordEntity.getKpkRecord()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading KyberPreKeyRecord", e);
            return null;
        }
        return null;
    }

    public List<KyberPreKeyRecord> loadKyberPreKeys() {
        List<KyberPreKeyRecordEntity> kyberPreKeyRecordEntities = kyberPreKeyRecordDao.loadKyberPreKeys();

        return kyberPreKeyRecordEntities
                .stream()
                .map(kyberPreKeyRecordEntity ->
                {
                    try {
                        return new KyberPreKeyRecord(base64Decoder.decode(kyberPreKeyRecordEntity.getKpkRecord()));
                    } catch (InvalidMessageException e) {
                        Log.e(TAG, "Error loading KyberPreKeyRecord", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void clearStorage() {
        kyberPreKeyRecordDao.clear();
    }
}
