package io.sekretess.cryptography.storage;

import android.util.Log;

import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyStore;

import io.sekretess.repository.PreKeyRepository;

public class SekretessPreKeyStore implements PreKeyStore {
    private final String TAG = SekretessPreKeyStore.class.getName();
    private final PreKeyRepository preKeyRepository;

    public SekretessPreKeyStore(PreKeyRepository preKeyRepository) {
        this.preKeyRepository = preKeyRepository;
    }

    @Override
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
        return preKeyRepository.loadPreKey(preKeyId);
    }

    @Override
    public void storePreKey(int preKeyId, PreKeyRecord record) {
        preKeyRepository.storePreKeyRecord(record);
    }

    @Override
    public boolean containsPreKey(int preKeyId) {
        try {
            return loadPreKey(preKeyId) != null;
        } catch (Exception e) {
            Log.e(TAG, "Error occurred during check PreKey exists", e);
            return false;
        }
    }

    @Override
    public void removePreKey(int preKeyId) {
        preKeyRepository.removePreKeyRecord(preKeyId);
    }

    public void clearStorage() {
        preKeyRepository.clearStorage();
    }

    public int count(){
        return preKeyRepository.count();
    }
}
