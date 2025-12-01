package io.sekretess.dto;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

public class KeyBundle {
    private int registrationId;
    private PreKeyRecord[] opk;
    private IdentityKeyPair identityKeyPair;
    private SignedPreKeyRecord signedPreKeyRecord;
    private KyberPreKeyRecord[] kyberPreKeyRecords;
    private byte[] signature;
//    private KyberPreKeyRecord lastResortKyberPreKey;
//    private int lastResortKyberPreKeyId;
//    private byte[] lastResortKeyberPreKeySignature;

    public KeyBundle() {

    }

    public KeyBundle(int registrationId, PreKeyRecord[] preKeyRecords,
                     SignedPreKeyRecord signedPreKeyRecord, IdentityKeyPair identityKeyPair,
                     byte[] signature, KyberPreKeyRecord[] kyberPreKeyRecords) {
        this.registrationId = registrationId;
        this.opk = preKeyRecords;
        this.signedPreKeyRecord = signedPreKeyRecord;
        this.identityKeyPair = identityKeyPair;
        this.signature = signature;
        this.kyberPreKeyRecords = kyberPreKeyRecords;
//        this.lastResortKyberPreKey = lastResortKyberPreKey;
//        this.lastResortKeyberPreKeySignature = lastResortKeyberPreKeySignature;
//        this.lastResortKyberPreKeyId = lastResortKyberPreKeyId;
    }

    public KeyBundle(PreKeyRecord[] opk) {
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

    public KyberPreKeyRecord[] getKyberPreKeyRecords() {
        return kyberPreKeyRecords;
    }

//    public KyberPreKeyRecord getLastResortKyberPreKey() {
//        return lastResortKyberPreKey;
//    }
//
//    public int getLastResortKyberPreKeyId() {
//        return lastResortKyberPreKeyId;
//    }
//
//    public byte[] getLastResortKeyberPreKeySignature() {
//        return lastResortKeyberPreKeySignature;
//    }
}
