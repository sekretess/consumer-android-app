package io.sekretess.dto;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

public class KeyMaterial {
    private int registrationId;
    private PreKeyRecord[] opk;
    private IdentityKeyPair identityKeyPair;
    private SignedPreKeyRecord signedPreKeyRecord;
    private KyberPreKeyRecord[] serializedKyberPreKeys;
    private byte[] signature;
    private KyberPreKeyRecord lastResortKyberPreKey;
    private int lastResortKyberPreKeyId;
    private byte[] lastResortKeyberPreKeySignature;

    public KeyMaterial() {

    }

    public KeyMaterial(int registrationId, PreKeyRecord[] preKeyRecords,
                       SignedPreKeyRecord signedPreKeyRecord, IdentityKeyPair identityKeyPair,
                       byte[] signature, KyberPreKeyRecord[] kyberPreKeyRecords,
                       KyberPreKeyRecord lastResortKyberPreKey,
                       byte[] lastResortKeyberPreKeySignature, int lastResortKyberPreKeyId) {
        this.registrationId = registrationId;
        this.opk = preKeyRecords;
        this.signedPreKeyRecord = signedPreKeyRecord;
        this.identityKeyPair = identityKeyPair;
        this.signature = signature;
        this.serializedKyberPreKeys = kyberPreKeyRecords;
        this.lastResortKyberPreKey = lastResortKyberPreKey;
        this.lastResortKeyberPreKeySignature = lastResortKeyberPreKeySignature;
        this.lastResortKyberPreKeyId = lastResortKyberPreKeyId;
    }

    public KeyMaterial(PreKeyRecord[] opk) {
        this.opk = opk;
    }

    public PreKeyRecord[] getOpk() {
        return opk;
    }

    public SignedPreKeyRecord getSignedPreKeyRecord() {
        return signedPreKeyRecord;
    }

    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyPair;
    }

    public byte[] getSignature() {
        return signature;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public KyberPreKeyRecord[] getSerializedKyberPreKeys() {
        return serializedKyberPreKeys;
    }

    public KyberPreKeyRecord getLastResortKyberPreKey() {
        return lastResortKyberPreKey;
    }

    public int getLastResortKyberPreKeyId() {
        return lastResortKyberPreKeyId;
    }

    public byte[] getLastResortKeyberPreKeySignature() {
        return lastResortKeyberPreKeySignature;
    }
}
