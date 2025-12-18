package io.sekretess.cryptography.storage;

import android.util.Log;

import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.KyberPreKeyStore;

import java.util.List;

import io.sekretess.db.repository.KyberPreKeyRepository;

public class SekretessKyberPreKeyStore implements KyberPreKeyStore {
    private final String TAG = SekretessKyberPreKeyStore.class.getName();
    private final KyberPreKeyRepository kyberPreKeyRepository;

    public SekretessKyberPreKeyStore(KyberPreKeyRepository kyberPreKeyRepository) {
        this.kyberPreKeyRepository = kyberPreKeyRepository;
    }

    @Override
    public KyberPreKeyRecord loadKyberPreKey(int kyberPreKeyId) throws InvalidKeyIdException {
        return kyberPreKeyRepository.loadKyberPreKey(kyberPreKeyId);
    }

    @Override
    public List<KyberPreKeyRecord> loadKyberPreKeys() {
        return kyberPreKeyRepository.loadKyberPreKeys();
    }

    @Override
    public void storeKyberPreKey(int kyberPreKeyId, KyberPreKeyRecord record) {
        kyberPreKeyRepository.storeKyberPreKey(record);
    }

    @Override
    public boolean containsKyberPreKey(int kyberPreKeyId) {
        try {
            return loadKyberPreKey(kyberPreKeyId) != null;
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while check KyberPreKey exists", e);
            return false;
        }
    }

    @Override
    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        kyberPreKeyRepository.markKyberPreKeyUsed(kyberPreKeyId);
    }

    public void clearStorage() {
        kyberPreKeyRepository.clearStorage();

    }
}
