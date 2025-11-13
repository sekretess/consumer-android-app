package io.sekretess.cryptography.storage;

import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;
import org.signal.libsignal.protocol.groups.state.SenderKeyStore;

import java.util.UUID;

import io.sekretess.repository.SenderKeyRepository;

public class SekretessSenderKeyStore implements SenderKeyStore {
    private final SenderKeyRepository senderKeyRepository;

    public SekretessSenderKeyStore(SenderKeyRepository senderKeyRepository) {
        this.senderKeyRepository = senderKeyRepository;
    }

    @Override
    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {
        this.senderKeyRepository.storeSenderKey(sender, distributionId, record);
    }

    @Override
    public SenderKeyRecord loadSenderKey(SignalProtocolAddress sender, UUID distributionId) {
        return this.senderKeyRepository.loadSenderKey(sender, distributionId);
    }

    public void clearStorage() {
        senderKeyRepository.clearStorage();
    }
}
