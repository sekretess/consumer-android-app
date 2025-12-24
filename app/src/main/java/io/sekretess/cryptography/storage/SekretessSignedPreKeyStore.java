package io.sekretess.cryptography.storage;

import android.util.Log;

import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyStore;

import java.util.List;

import io.sekretess.db.repository.PreKeyRepository;
import io.sekretess.db.repository.SignedPreKeyRepository;

public class SekretessSignedPreKeyStore implements SignedPreKeyStore {
    private final String TAG = SekretessSignedPreKeyStore.class.getName();
    private final PreKeyRepository preKeyRepository;
    private final SignedPreKeyRepository signedPreKeyRepository;

    public SekretessSignedPreKeyStore(PreKeyRepository preKeyRepository, SignedPreKeyRepository signedPreKeyRepository) {
        this.preKeyRepository = preKeyRepository;
        this.signedPreKeyRepository = signedPreKeyRepository;
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        return signedPreKeyRepository.getSignedPreKeyRecord(signedPreKeyId);
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        return signedPreKeyRepository.loadSignedPreKeys();
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
        signedPreKeyRepository.storeSignedPreKeyRecord(record);
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
        signedPreKeyRepository.removeSignedPreKey(signedPreKeyId);
    }

    public void clearStorage() {
        preKeyRepository.clearStorage();
    }
}
