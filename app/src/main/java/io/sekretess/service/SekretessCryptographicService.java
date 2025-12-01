package io.sekretess.service;

import android.util.Log;
import android.widget.Toast;

import org.signal.libsignal.protocol.DuplicateMessageException;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.LegacyMessageException;
import org.signal.libsignal.protocol.NoSessionException;
import org.signal.libsignal.protocol.SessionCipher;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.UsePqRatchet;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.ecc.ECPrivateKey;
import org.signal.libsignal.protocol.groups.GroupCipher;
import org.signal.libsignal.protocol.groups.GroupSessionBuilder;
import org.signal.libsignal.protocol.kem.KEMKeyPair;
import org.signal.libsignal.protocol.kem.KEMKeyType;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.signal.libsignal.protocol.message.SenderKeyDistributionMessage;
import org.signal.libsignal.protocol.state.KyberPreKeyRecord;
import org.signal.libsignal.protocol.state.PreKeyRecord;
import org.signal.libsignal.protocol.state.SignedPreKeyRecord;
import org.signal.libsignal.protocol.util.Medium;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

import io.sekretess.SekretessApplication;
import io.sekretess.dto.KeyBundle;
import io.sekretess.cryptography.storage.SekretessSignalProtocolStore;
import io.sekretess.utils.ApiClient;


public class SekretessCryptographicService {
    private final int SIGNAL_KEY_COUNT = 15;
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private final SekretessSignalProtocolStore sekretessSignalProtocolStore;
    private final ApiClient apiClient;
    private final String TAG = "SekretessCryptographicService";
    private final int deviceId = 1;
    private final SekretessApplication application;

    public SekretessCryptographicService(SekretessApplication application,
                                         SekretessSignalProtocolStore sekretessSignalProtocolStore,
                                         ApiClient apiClient) {
        this.application = application;
        this.sekretessSignalProtocolStore = sekretessSignalProtocolStore;
        this.apiClient = apiClient;
    }

    public void updateOneTimeKeys() {
        IdentityKeyPair identityKeyPair = sekretessSignalProtocolStore.getIdentityKeyPair();
        PreKeyRecord[] preKeyRecords = generatePreKeys();
        KyberPreKeyRecord[] kyberPreKeyRecords = generateKyberPreKeys(identityKeyPair.getPrivateKey());
        try {
            if (apiClient.updateOneTimeKeys(preKeyRecords, kyberPreKeyRecords)) {
                storePreKeyRecords(preKeyRecords);
                storeKyberPreKeyRecords(kyberPreKeyRecords);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during update one time keys", e);
            Toast.makeText(application.getApplicationContext(), "Error during update one time keys" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private KyberPreKeyRecord[] generateKyberPreKeys(ECPrivateKey ecPrivateKey) {
        // Generate post quantum resistance keys + 1 last resort key
        KyberPreKeyRecord[] kyberPreKeyRecords = new KyberPreKeyRecord[SIGNAL_KEY_COUNT + 1];

        for (int i = 0; i < kyberPreKeyRecords.length; i++) {
            KyberPreKeyRecord kyberPreKeyRecord = generateKyberPreKey(ecPrivateKey);
            kyberPreKeyRecords[i] = kyberPreKeyRecord;
        }
        // Generated post quantum keys
        return kyberPreKeyRecords;
    }

    private KyberPreKeyRecord generateKyberPreKey(ECPrivateKey ecPrivateKey) {
        int kyberSignedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        KEMKeyPair kemKeyPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024);
        KyberPreKeyRecord kyberPreKeyRecord = new KyberPreKeyRecord(kyberSignedPreKeyId,
                System.currentTimeMillis(), kemKeyPair,
                ecPrivateKey.calculateSignature(kemKeyPair.getPublicKey().serialize()));
        return kyberPreKeyRecord;
    }

    public void processKeyDistributionMessage(String name, String base64Key) {
        try {
            Log.i(TAG, "base64 keyDistributionMessage: " + base64Key);
            SenderKeyDistributionMessage senderKeyDistributionMessage =
                    new SenderKeyDistributionMessage(Base64.getDecoder().decode(base64Key));
            new GroupSessionBuilder(sekretessSignalProtocolStore)
                    .process(new SignalProtocolAddress(name, 1), senderKeyDistributionMessage);
            Log.i(TAG, "Group chat chipper created and stored : " + name);
        } catch (Exception e) {
            Log.e(TAG, "Error during decrypt key distribution message", e);
            Toast.makeText(application.getApplicationContext(),
                    "Error during decrypt distribution message" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void storeKyberPreKeyRecords(KyberPreKeyRecord[] kyberPreKeyRecords) {
        for (KyberPreKeyRecord kyberPreKeyRecord : kyberPreKeyRecords) {
            sekretessSignalProtocolStore.storeKyberPreKey(kyberPreKeyRecord.getId(), kyberPreKeyRecord);
        }
    }

    private void storePreKeyRecords(PreKeyRecord[] preKeyRecords) {
        for (PreKeyRecord preKeyRecord : preKeyRecords) {
            sekretessSignalProtocolStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
        }
    }

    private void storeSignedPreKey(SignedPreKeyRecord signedPreKeyRecord) {
        sekretessSignalProtocolStore.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);
    }

    private SignedPreKeyRecord generateSignedPreKey(ECKeyPair keyPair, byte[] signature) {
        //Generate signed prekeyRecord
        int signedPreKeyId = new Random().nextInt(Medium.MAX_VALUE - 1);
        return new SignedPreKeyRecord(signedPreKeyId, System.currentTimeMillis(), keyPair, signature);
    }

    private PreKeyRecord[] generatePreKeys() {
        PreKeyRecord[] preKeyRecords = new PreKeyRecord[SIGNAL_KEY_COUNT];
        SecureRandom preKeyRecordIdGenerator = new SecureRandom();
        for (int i = 0; i < preKeyRecords.length; i++) {
            int id = preKeyRecordIdGenerator.nextInt(Integer.MAX_VALUE);
            ECKeyPair ecKeyPair = ECKeyPair.generate();
            PreKeyRecord preKeyRecord = new PreKeyRecord(id, ecKeyPair);
            preKeyRecords[i] = preKeyRecord;
        }
        return preKeyRecords;
    }

    public KeyBundle initializeKeyBundle() {
        sekretessSignalProtocolStore.clearStorage();

        ECKeyPair signedPreKeyPair = ECKeyPair.generate();
        IdentityKeyPair identityKeyPair = sekretessSignalProtocolStore.getIdentityKeyPair();
        int registrationId = sekretessSignalProtocolStore.getLocalRegistrationId();

        byte[] signature = identityKeyPair.getPrivateKey().calculateSignature(signedPreKeyPair
                .getPublicKey().serialize());

        //Generate one-time prekeys
        PreKeyRecord[] opk = generatePreKeys();

        SignedPreKeyRecord signedPreKeyRecord = generateSignedPreKey(signedPreKeyPair, signature);
        KyberPreKeyRecord[] kyberPreKeyRecords = generateKyberPreKeys(identityKeyPair.getPrivateKey());

        return new KeyBundle(registrationId, opk, signedPreKeyRecord,
                identityKeyPair, signature,
                kyberPreKeyRecords);
    }


    public boolean init() throws Exception {
        if (sekretessSignalProtocolStore.registrationRequired()) {
            KeyBundle keyBundle = initializeKeyBundle();

            if (apiClient.upsertKeyStore(keyBundle)) {
                sekretessSignalProtocolStore.clearStorage();
                storeKyberPreKeyRecords(keyBundle.getKyberPreKeyRecords());
                storePreKeyRecords(keyBundle.getOpk());
                storeSignedPreKey(keyBundle.getSignedPreKeyRecord());
                return true;
            } else {
                sekretessSignalProtocolStore.clearStorage();
                return false;
            }
        } else if (sekretessSignalProtocolStore.updateKeysRequired()) {
            updateOneTimeKeys();
        }
        return true;
    }

    public Optional<String> decryptGroupChatMessage(String sender, String base64Message) {
        try {
            GroupCipher groupCipher = new GroupCipher(sekretessSignalProtocolStore, new SignalProtocolAddress(sender, 1));
            return Optional.of(new String(groupCipher.decrypt(base64Decoder.decode(base64Message))));
        } catch (DuplicateMessageException | LegacyMessageException | InvalidMessageException |
                 NoSessionException duplicateMessageException) {
            Log.e(TAG, "Error occurred while decrypting group chat message. Sender: " + sender);
            return Optional.empty();
        }
    }

    public Optional<String> decryptPrivateMessage(String sender, String base64Message) {
        try {
            PreKeySignalMessage preKeySignalMessage = new PreKeySignalMessage(base64Decoder.decode(base64Message));
            SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(sender, 1);
            SessionCipher sessionCipher = new SessionCipher(sekretessSignalProtocolStore, signalProtocolAddress);
            return Optional.of(new String(sessionCipher.decrypt(preKeySignalMessage, UsePqRatchet.YES)));
        } catch (Exception e) {
            Log.e(TAG, "Error during decrypt private message", e);
            Toast.makeText(application.getApplicationContext(),
                    "Error during decrypt private message" + e.getMessage(), Toast.LENGTH_LONG).show();
            return Optional.empty();
        }
    }
}
