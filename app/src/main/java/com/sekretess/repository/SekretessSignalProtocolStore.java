package com.sekretess.repository;

import android.content.Context;

import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidKeyIdException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;

import java.util.List;
import java.util.UUID;

public class SekretessSignalProtocolStore extends InMemorySignalProtocolStore {
    private final DbHelper dbHelper;

    public SekretessSignalProtocolStore(Context context, IdentityKeyPair identityKeyPair, int registrationId) {
        super(identityKeyPair, registrationId);
        this.dbHelper = new DbHelper(context);
    }

    @Override
    public void removePreKey(int preKeyId) {
        super.removePreKey(preKeyId);
        dbHelper.removePreKeyRecord(preKeyId);
    }

    @Override
    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {
        super.storeSenderKey(sender, distributionId, record);
        dbHelper.storeSenderKey(sender, distributionId, record);
    }


    @Override
    public void markKyberPreKeyUsed(int kyberPreKeyId) {
        super.markKyberPreKeyUsed(kyberPreKeyId);
        dbHelper.markKyberPreKeyUsed(kyberPreKeyId);
    }

    @Override
    public void storeKyberPreKey(int kyberPreKeyId, KyberPreKeyRecord record) {
        super.storeKyberPreKey(kyberPreKeyId, record);
        dbHelper.storeKyberPreKey(record);
    }

    @Override
    public void storeSession(SignalProtocolAddress address, SessionRecord record) {
        super.storeSession(address, record);
        dbHelper.storeSession(address, record);

    }

    @Override
    public void deleteSession(SignalProtocolAddress address) {
        super.deleteSession(address);
        dbHelper.removeSession(address);
    }
}
