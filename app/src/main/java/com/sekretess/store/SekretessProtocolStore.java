package com.sekretess.store;

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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class SekretessProtocolStore implements SignalProtocolStore {
    private final KeyStore keyStore ;

    public SekretessProtocolStore(IdentityKeyPair identityKeyPair, int registrationId) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        this.keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Enumeration<String> aliases = this.keyStore.aliases();
        while(aliases.hasMoreElements()){
            System.out.println(aliases.nextElement());
        }
    }

    @Override
    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {

    }

    @Override
    public SenderKeyRecord loadSenderKey(SignalProtocolAddress sender, UUID distributionId) {
        return null;
    }

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        return null;
    }

    @Override
    public int getLocalRegistrationId() {
        return 0;
    }

    @Override
    public boolean saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        return false;
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {
        return false;
    }

    @Override
    public IdentityKey getIdentity(SignalProtocolAddress address) {
        return null;
    }

    @Override
    public KyberPreKeyRecord loadKyberPreKey(int kyberPreKeyId) throws InvalidKeyIdException {
        return null;
    }

    @Override
    public List<KyberPreKeyRecord> loadKyberPreKeys() {
        return null;
    }

    @Override
    public void storeKyberPreKey(int kyberPreKeyId, KyberPreKeyRecord record) {

    }

    @Override
    public boolean containsKyberPreKey(int kyberPreKeyId) {
        return false;
    }

    @Override
    public void markKyberPreKeyUsed(int kyberPreKeyId) {

    }

    @Override
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
        return null;
    }

    @Override
    public void storePreKey(int preKeyId, PreKeyRecord record) {

    }

    @Override
    public boolean containsPreKey(int preKeyId) {
        return false;
    }

    @Override
    public void removePreKey(int preKeyId) {

    }

    @Override
    public SessionRecord loadSession(SignalProtocolAddress address) {
        return null;
    }

    @Override
    public List<SessionRecord> loadExistingSessions(List<SignalProtocolAddress> addresses) throws NoSessionException {
        return null;
    }

    @Override
    public List<Integer> getSubDeviceSessions(String name) {
        return null;
    }

    @Override
    public void storeSession(SignalProtocolAddress address, SessionRecord record) {

    }

    @Override
    public boolean containsSession(SignalProtocolAddress address) {
        return false;
    }

    @Override
    public void deleteSession(SignalProtocolAddress address) {

    }

    @Override
    public void deleteAllSessions(String name) {

    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        return null;
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        return null;
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {

    }

    @Override
    public boolean containsSignedPreKey(int signedPreKeyId) {
        return false;
    }

    @Override
    public void removeSignedPreKey(int signedPreKeyId) {

    }
}
