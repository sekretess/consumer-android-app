package io.sekretess.dto;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

public class KeyMaterial {
    private int registrationId;
    private String[] opk;
    private IdentityKeyPair identityKeyPair;
    private SignedPreKeyRecord signedPreKeyRecord;
    private String[] serializedKyberPreKeys;
    private byte[] signature;
    private String lastResortKyberPreKey;
    private int lastResortKyberPreKeyId;

    public KeyMaterial() {

    }

    public KeyMaterial(int registrationId, String[] serializedOnetimePreKeys,
                       SignedPreKeyRecord signedPreKeyRecord, IdentityKeyPair identityKeyPair,
                       byte[] signature, String[] serializedKyberPreKeys, String lastResortKyberPreKey,
                       int lastResortKyberPreKeyId) {
        this.registrationId = registrationId;
        this.opk = serializedOnetimePreKeys;
        this.signedPreKeyRecord = signedPreKeyRecord;
        this.identityKeyPair = identityKeyPair;
        this.signature = signature;
        this.serializedKyberPreKeys = serializedKyberPreKeys;
        this.lastResortKyberPreKey = lastResortKyberPreKey;
        this.lastResortKyberPreKeyId = lastResortKyberPreKeyId;
    }

    public KeyMaterial(String[] opk) {
        this.opk = opk;
    }

    public String[] getOpk() {
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

    public String[] getSerializedKyberPreKeys() {
        return serializedKyberPreKeys;
    }

    public String getLastResortKyberPreKey() {
        return lastResortKyberPreKey;
    }

    public int getLastResortKyberPreKeyId() {
        return lastResortKyberPreKeyId;
    }
}
