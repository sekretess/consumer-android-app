package com.sekretess.dto;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

public class KeyMaterial {
    private int registrationId;
    private String[] opk;
    private IdentityKeyPair identityKeyPair;
    private SignedPreKeyRecord signedPreKeyRecord;

    private byte[] signature;

    public KeyMaterial() {

    }

    public KeyMaterial(int registrationId, String[] opk, SignedPreKeyRecord signedPreKeyRecord,
                       IdentityKeyPair identityKeyPair, byte[] signature) {
        this.registrationId = registrationId;
        this.opk = opk;
        this.signedPreKeyRecord = signedPreKeyRecord;
        this.identityKeyPair = identityKeyPair;
        this.signature = signature;
    }

    public KeyMaterial(String[] opk){
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
}
