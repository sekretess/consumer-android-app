package io.sekretess.cryptography.storage;

import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.IdentityKeyStore;
import org.signal.libsignal.protocol.util.KeyHelper;

import io.sekretess.db.repository.IdentityKeyRepository;
import io.sekretess.db.repository.RegistrationRepository;

public class SekretessIdentityKeyStore implements IdentityKeyStore {

    private final IdentityKeyRepository identityKeyRepository;
    private final RegistrationRepository registrationRepository;

    public SekretessIdentityKeyStore(IdentityKeyRepository identityKeyRepository, RegistrationRepository registrationRepository) {
        this.identityKeyRepository = identityKeyRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        IdentityKeyPair identityKeyPair = identityKeyRepository.getIdentityKeyPair();
        if (identityKeyPair == null) {
            identityKeyPair = IdentityKeyPair.generate();
            identityKeyRepository.storeIdentityKeyPair(identityKeyPair);
        }
        return identityKeyPair;
    }

    @Override
    public int getLocalRegistrationId() {
        int registrationId = registrationRepository.getRegistrationId();
        if (registrationId == 0) {
            registrationId = KeyHelper.generateRegistrationId(false);
            registrationRepository.storeRegistrationId(registrationId);
        }
        return registrationId;
    }

    @Override
    public IdentityChange saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        return identityKeyRepository.saveIdentity(address, identityKey);
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
        if (direction == Direction.RECEIVING) {
            return true;
        } else {
            IdentityKey trustedIdentity = getIdentity(address);
            return trustedIdentity == null || trustedIdentity.equals(identityKey);
        }
    }

    @Override
    public IdentityKey getIdentity(SignalProtocolAddress address) {
        return identityKeyRepository.getIdentity(address);
    }

    public boolean registrationRequired() {
        return !(registrationRepository.getRegistrationId() > 0 && identityKeyRepository != null);
    }

    public void clearStorage() {
        if (identityKeyRepository != null) {
            identityKeyRepository.clearStorage();
        }
        if (registrationRepository != null) {
            registrationRepository.clearStorage();
        }
    }
}
