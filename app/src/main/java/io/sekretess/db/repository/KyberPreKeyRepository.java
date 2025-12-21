package io.sekretess.db.repository;

import android.util.Log;

import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.sekretess.db.SekretessDatabase;
import io.sekretess.db.dao.KyberPreKeyDao;
import io.sekretess.db.model.KyberPreKeyEntity;
import io.sekretess.dependency.SekretessDependencyProvider;

public class KyberPreKeyRepository {
    private final KyberPreKeyDao kyberPreKeyDao;
    private final String TAG = KyberPreKeyRepository.class.getName();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();


    public KyberPreKeyRepository() {
        SekretessDatabase sekretessDatabase = SekretessDatabase
                .getInstance(SekretessDependencyProvider.applicationContext());
        this.kyberPreKeyDao = sekretessDatabase.kyberPreKeyRecordDao();
    }


    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        kyberPreKeyDao.markUsed(kyberPreKeyId);
    }

    public void storeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) {
        KyberPreKeyEntity kyberPreKeyEntity
                = new KyberPreKeyEntity(kyberPreKeyRecord.getId(),
                base64Encoder.encodeToString(kyberPreKeyRecord.getSignature()), System.currentTimeMillis());
        kyberPreKeyDao.insert(kyberPreKeyEntity);
    }

    public KyberPreKeyRecord loadKyberPreKey(int kyberPreKeyId) {
        KyberPreKeyEntity kyberPreKeyEntity = kyberPreKeyDao
                .loadKyberPreKey(kyberPreKeyId);
        try {
            if (kyberPreKeyEntity != null) {
                return new KyberPreKeyRecord(base64Decoder.decode(kyberPreKeyEntity.getKpkRecord()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading KyberPreKeyRecord", e);
            return null;
        }
        return null;
    }

    public List<KyberPreKeyRecord> loadKyberPreKeys() {
        List<KyberPreKeyEntity> kyberPreKeyRecordEntities = kyberPreKeyDao.loadKyberPreKeys();

        return kyberPreKeyRecordEntities
                .stream()
                .map(kyberPreKeyEntity ->
                {
                    try {
                        return new KyberPreKeyRecord(base64Decoder.decode(kyberPreKeyEntity.getKpkRecord()));
                    } catch (InvalidMessageException e) {
                        Log.e(TAG, "Error loading KyberPreKeyRecord", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void clearStorage() {
        kyberPreKeyDao.clear();
    }
}
