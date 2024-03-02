package com.sekretess.dto;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;

public class SignalKeys {
    private IdentityKeyPair ipk;
    private SignedPreKeyRecord spk;

    private String[] opks;
}
