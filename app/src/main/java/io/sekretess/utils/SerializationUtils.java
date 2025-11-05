package io.sekretess.utils;

import org.signal.libsignal.protocol.InvalidKeyException;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;

import java.util.Base64;

public class SerializationUtils {

    private SerializationUtils() {

    }

    private static final Base64.Encoder base64Encoder = Base64.getEncoder();

    public static String[] serializeSignedPreKeys(PreKeyRecord[] preKeyRecords) throws InvalidKeyException {
        String[] serializedOneTimePreKeys = new String[preKeyRecords.length];

        int idx = 0;
        for (PreKeyRecord preKeyRecord : preKeyRecords) {
            serializedOneTimePreKeys[idx++] = preKeyRecord.getId() + ":" + base64Encoder.encodeToString(preKeyRecord.getKeyPair().getPublicKey().serialize());
        }
        return serializedOneTimePreKeys;
    }

    public static String serializeKyberPreKey(KyberPreKeyRecord kyberPreKeyRecord) throws InvalidKeyException {
        return kyberPreKeyRecord.getId() + ":" + base64Encoder.encodeToString(kyberPreKeyRecord.getKeyPair().getPublicKey().serialize()) + ":" + base64Encoder.encodeToString(kyberPreKeyRecord.getSignature());
    }

    public static String[] serializeKyberPreKeys(KyberPreKeyRecord[] kyberPreKeyRecords) throws InvalidKeyException {
        String[] serializedKyberPreKeys = new String[kyberPreKeyRecords.length];
        int idx = 0;
        for (KyberPreKeyRecord kyberPreKeyRecord : kyberPreKeyRecords) {
            serializedKyberPreKeys[idx++] = serializeKyberPreKey(kyberPreKeyRecord);
        }
        return serializedKyberPreKeys;
    }
}
