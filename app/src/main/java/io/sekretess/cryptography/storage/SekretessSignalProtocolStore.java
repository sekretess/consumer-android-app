package io.sekretess.cryptography.storage;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.NoSessionException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.state.SignalProtocolStore;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.sekretess.SekretessApplication;
import io.sekretess.model.IdentityKeyPairStoreEntity;
import io.sekretess.model.KyberPreKeyRecordsEntity;
import io.sekretess.model.PreKeyRecordStoreEntity;
import io.sekretess.model.RegistrationIdStoreEntity;
import io.sekretess.model.SignedPreKeyRecordStoreEntity;
import io.sekretess.repository.IdentityKeyRepository;
import io.sekretess.repository.KyberPreKeyRepository;
import io.sekretess.repository.PreKeyRepository;
import io.sekretess.repository.RegistrationRepository;
import io.sekretess.repository.SenderKeyRepository;
import io.sekretess.repository.SessionRepository;

public class SekretessSignalProtocolStore implements SignalProtocolStore {
    private final SekretessIdentityKeyStore identityKeyStore;
    private final SekretessPreKeyStore preKeyStore;
    private final SekretessSessionStore sessionStore;

    private final SekretessSignedPreKeyStore signedPreKeyStore;

    private final SekretessSenderKeyStore senderKeyStore;

    private final SekretessKyberPreKeyStore kyberPreKeyStore;
    private int minKeysThreshold = 5;

    public SekretessSignalProtocolStore(SekretessApplication application,
                                        IdentityKeyRepository identityKeyRepository,
                                        RegistrationRepository registrationRepository,
                                        PreKeyRepository preKeyRepository,
                                        SessionRepository sessionRepository,
                                        SenderKeyRepository senderKeyRepository,
                                        KyberPreKeyRepository kyberPreKeyRepository) {
        this.identityKeyStore = new SekretessIdentityKeyStore(identityKeyRepository, registrationRepository);
        this.preKeyStore = new SekretessPreKeyStore(preKeyRepository);
        this.sessionStore = new SekretessSessionStore(sessionRepository);
        this.signedPreKeyStore = new SekretessSignedPreKeyStore(preKeyRepository);
        this.senderKeyStore = new SekretessSenderKeyStore(senderKeyRepository);
        this.kyberPreKeyStore = new SekretessKyberPreKeyStore(kyberPreKeyRepository);
    }

    public boolean registrationRequired() {
        return identityKeyStore.registrationRequired();
    }

    @Override
    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {
        senderKeyStore.storeSenderKey(sender, distributionId, record);
    }

    @Override
    public SenderKeyRecord loadSenderKey(SignalProtocolAddress sender, UUID distributionId) {
        return senderKeyStore.loadSenderKey(sender, distributionId);
    }

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyStore.getIdentityKeyPair();
    }

    @Override
    public int getLocalRegistrationId() {
        return identityKeyStore.getLocalRegistrationId();
    }

    @Override
    public IdentityChange saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        return identityKeyStore.saveIdentity(address, identityKey);
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
        return identityKeyStore.isTrustedIdentity(address, identityKey, direction);
    }

    @Override
    public IdentityKey getIdentity(SignalProtocolAddress address) {
        return identityKeyStore.getIdentity(address);
    }

    @Override
    public KyberPreKeyRecord loadKyberPreKey(int kyberPreKeyId) throws InvalidKeyIdException {
        return kyberPreKeyStore.loadKyberPreKey(kyberPreKeyId);
    }

    @Override
    public List<KyberPreKeyRecord> loadKyberPreKeys() {
        return kyberPreKeyStore.loadKyberPreKeys();
    }

    @Override
    public void storeKyberPreKey(int kyberPreKeyId, KyberPreKeyRecord record) {
        kyberPreKeyStore.storeKyberPreKey(kyberPreKeyId, record);
    }

    @Override
    public boolean containsKyberPreKey(int kyberPreKeyId) {
        return kyberPreKeyStore.containsKyberPreKey(kyberPreKeyId);
    }

    @Override
    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        kyberPreKeyStore.markKyberPreKeyUsed(kyberPreKeyId);
    }

    @Override
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
        return preKeyStore.loadPreKey(preKeyId);
    }

    @Override
    public void storePreKey(int preKeyId, PreKeyRecord record) {
        preKeyStore.storePreKey(preKeyId, record);
    }

    @Override
    public boolean containsPreKey(int preKeyId) {
        return preKeyStore.containsPreKey(preKeyId);
    }

    @Override
    public void removePreKey(int preKeyId) {
        preKeyStore.removePreKey(preKeyId);
    }

    @Override
    public SessionRecord loadSession(SignalProtocolAddress address) {
        return sessionStore.loadSession(address);
    }

    @Override
    public List<SessionRecord> loadExistingSessions(List<SignalProtocolAddress> addresses) throws NoSessionException {
        return sessionStore.loadExistingSessions(addresses);
    }

    @Override
    public List<Integer> getSubDeviceSessions(String name) {
        return sessionStore.getSubDeviceSessions(name);
    }

    @Override
    public void storeSession(SignalProtocolAddress address, SessionRecord record) {
        sessionStore.storeSession(address, record);
    }

    @Override
    public boolean containsSession(SignalProtocolAddress address) {
        return sessionStore.containsSession(address);
    }

    @Override
    public void deleteSession(SignalProtocolAddress address) {
        sessionStore.deleteSession(address);
    }

    @Override
    public void deleteAllSessions(String name) {
        sessionStore.deleteAllSessions(name);
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        return signedPreKeyStore.loadSignedPreKey(signedPreKeyId);
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        return signedPreKeyStore.loadSignedPreKeys();
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
        signedPreKeyStore.storeSignedPreKey(signedPreKeyId, record);
    }

    @Override
    public boolean containsSignedPreKey(int signedPreKeyId) {
        return signedPreKeyStore.containsSignedPreKey(signedPreKeyId);
    }

    @Override
    public void removeSignedPreKey(int signedPreKeyId) {
        signedPreKeyStore.removeSignedPreKey(signedPreKeyId);
    }

    public boolean clearStorage() {
        this.identityKeyStore.clearStorage();
        this.preKeyStore.clearStorage();
        this.sessionStore.clearStorage();
        this.signedPreKeyStore.clearStorage();
        this.senderKeyStore.clearStorage();
        this.kyberPreKeyStore.clearStorage();
        return true;
    }


    public boolean updateKeysRequired() {
        return preKeyStore.count()<= minKeysThreshold;
    }
}
