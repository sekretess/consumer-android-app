package io.sekretess.cryptography.storage;

import android.util.Log;

import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyStore;

import java.util.List;

import io.sekretess.db.repository.PreKeyRepository;

public class SekretessSignedPreKeyStore implements SignedPreKeyStore {
    private final String TAG = SekretessSignedPreKeyStore.class.getName();
    private final PreKeyRepository preKeyRepository;

    public SekretessSignedPreKeyStore(PreKeyRepository preKeyRepository) {
        this.preKeyRepository = preKeyRepository;
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        List<SignedPreKeyRecord> signedPreKeyRecords = loadSignedPreKeys();
        return preKeyRepository.getSignedPreKeyRecord(signedPreKeyId);
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        return preKeyRepository.loadSignedPreKeys();
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
        preKeyRepository.storeSignedPreKeyRecord(record);
    }

    @Override
    public boolean containsSignedPreKey(int signedPreKeyId) {
        try {
            return loadSignedPreKey(signedPreKeyId) != null;
        } catch (Exception e) {
            Log.e(TAG, "Error occurred during containsSignedPreKey", e);
            return false;
        }
    }

    @Override
    public void removeSignedPreKey(int signedPreKeyId) {
        preKeyRepository.removeSignedPreKey(signedPreKeyId);
    }

    public void clearStorage() {
        preKeyRepository.clearStorage();
    }
}
